package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.UserDetails
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.*
import java.time.Instant

class UserRepositoryTests : CleanDatabaseTests()
{
    @Test
    fun `can get user`()
    {
        val sut = OrderlyUserRepository()
        val then = Instant.now()
        sut.addUser("email@somewhere.com", "user.name", "full name", UserSource.GitHub)

        val result = sut.getUser("email@somewhere.com")!!
        assertThat(result.displayName).isEqualTo("full name")
        assertThat(result.username).isEqualTo("user.name")
        assertThat(result.email).isEqualTo("email@somewhere.com")
        assertThat(result.lastLoggedIn).isGreaterThanOrEqualTo(then)
        assertThat(result.source).isEqualTo(UserSource.GitHub.toString())

        val nullResult = sut.getUser("nonsense")
        assertThat(nullResult).isNull()
    }

    @Test
    fun `gets user emails in alphabetical order`()
    {
        val sut = OrderlyUserRepository()

        insertUser("c@somewhere.com", "some.name")
        insertUser("a@somewhere.com", "test.name")
        insertUser("b@somewhere.com", "test.name")

        val result = sut.getUserEmails()
        assertThat(result).containsExactlyElementsOf(listOf("a@somewhere.com", "b@somewhere.com", "c@somewhere.com"))
    }

    @Test
    fun `addUser can create new github user`()
    {
        val then = Instant.now()
        val sut = OrderlyUserRepository()
        sut.addUser("email@somewhere.com", "user.name", "full name", UserSource.GitHub)

        val result = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER)
                    .where(ORDERLYWEB_USER.USERNAME.eq("user.name"))
                    .fetchOneInto(UserDetails::class.java)
        }

        assertThat(result.email).isEqualTo("email@somewhere.com")
        assertThat(result.source).isEqualTo("GitHub")
        assertThat(result.displayName).isEqualTo("full name")
        assertThat(result.lastLoggedIn).isBetween(then, Instant.now())
    }

    @Test
    fun `addUser adds user group and adds user to group`()
    {
        val email = "email@somewhere.com"
        val sut = OrderlyUserRepository()
        sut.addUser(email, "user.name", "full name", UserSource.GitHub)

        val result = JooqContext().use {
            it.dsl.select(ORDERLYWEB_USER_GROUP.ID)
                    .from(ORDERLYWEB_USER_GROUP)
                    .fetchOneInto(String::class.java)
        }

        assertThat(result).isEqualTo(email)

        val userResult = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER_GROUP_USER)
                    .fetchOne()
        }

        assertThat(userResult[ORDERLYWEB_USER_GROUP_USER.EMAIL]).isEqualTo(email)
        assertThat(userResult[ORDERLYWEB_USER_GROUP_USER.USER_GROUP]).isEqualTo(email)
    }

    @Test
    fun `addUser updates source, username, display name and last logged in, if email already exists`()
    {
        val sut = OrderlyUserRepository()
        sut.addUser("email@somewhere.com", "user.name", "User Name", UserSource.Montagu)

        var result = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER)
                    .where(ORDERLYWEB_USER.EMAIL.eq("email@somewhere.com"))
                    .fetchOneInto(UserDetails::class.java)
        }

        assertThat(result.username).isEqualTo("user.name")
        assertThat(result.source).isEqualTo("Montagu")

        val then = Instant.now()
        sut.addUser("email@somewhere.com", "another.name", "Another Name", UserSource.GitHub)

        result = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER)
                    .where(ORDERLYWEB_USER.EMAIL.eq("email@somewhere.com"))
                    .fetchOneInto(UserDetails::class.java)
        }

        assertThat(result.username).isEqualTo("another.name")
        assertThat(result.displayName).isEqualTo("Another Name")
        assertThat(result.source).isEqualTo("GitHub")
        assertThat(result.lastLoggedIn).isBetween(then, Instant.now())
    }


    @Test
    fun `getIndividualReportReadersForReport gets readers with correctly scoped identity permissions`()
    {
        JooqContext().use {
            insertReport("report1", "version1")
            insertReport("report2", "version2")

            insertUser("global.reader@email.com", "global.reader.name")
            giveUserGroupPermission("global.reader@email.com", "reports.read", Scope.Global())

            insertUser("scoped.reader@email.com", "scoped.reader.name")
            giveUserGroupPermission("scoped.reader@email.com", "reports.read", Scope.Specific("report", "report1"))

            insertUser("another.scoped.reader@email.com", "scoped.reader.name")
            giveUserGroupPermission("another.scoped.reader@email.com", "reports.read", Scope.Specific("report", "report2"))

            insertUser("non.reader@email.com", "non.reader.name")
        }

        val sut = OrderlyUserRepository()
        val result = sut.getScopedReportReaderUsers("report1")

        assertThat(result.count()).isEqualTo(1)
        assertThat(result[0].username).isEqualTo("scoped.reader.name")
        assertThat(result[0].email).isEqualTo("scoped.reader@email.com")
        assertThat(result[0].displayName).isEqualTo("scoped.reader.name")
    }

    @Test
    fun `getIndividualReportReadersForReport does not get readers with global identity permissions`()
    {
        JooqContext().use {
            insertUser("global.reader@email.com", "global.reader.name")
            giveUserGroupPermission("global.reader@email.com", "reports.read", Scope.Global())
        }

        val sut = OrderlyUserRepository()
        val result = sut.getScopedReportReaderUsers("report1")
        assertThat(result.count()).isEqualTo(0)
    }

    @Test
    fun `getIndividualReportReadersForReport does not get all readers with permissions from non-identity groups`()
    {
        val globalRead = ReifiedPermission("reports.read", Scope.Global())
        val report1Read = ReifiedPermission("reports.read", Scope.Specific("report", "report1"))
        val report2Read = ReifiedPermission("reports.read", Scope.Specific("report", "report2"))

        insertReport("report1", "version1")
        insertReport("report2", "version2")

        createGroup("global.readers", globalRead)
        createGroup("report1.readers", report1Read)
        createGroup("report2.readers", report2Read)

        addMembers("global.readers", "global@reader.com", "all.groups@reader.com")
        addMembers("report1.readers", "report1@reader.com")
        addMembers("report2.readers", "report2@reader.com")

        giveUserGroupMember("report1.readers", "all.groups@reader.com")
        giveUserGroupMember("report2.readers", "all.groups@reader.com")

        val sut = OrderlyUserRepository()
        val result = sut.getScopedReportReaderUsers("report1")

        assertThat(result.count()).isEqualTo(0)
    }
}