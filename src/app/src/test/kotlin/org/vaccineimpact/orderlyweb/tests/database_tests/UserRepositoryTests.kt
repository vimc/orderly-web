package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
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
    fun `getReportReaders gets all readers with identity permissions`()
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
        val result = sut.getReportReaders("report1").toSortedMap(compareBy { it.username })

        assertThat(result.count()).isEqualTo(2)

        assertThat(result.firstKey().username).isEqualTo("global.reader.name")
        val globalUserPermissions = result[result.firstKey()]!!
        assertThat(globalUserPermissions.count()).isEqualTo(1)
        assertThat(globalUserPermissions[0].userGroup).isEqualTo("global.reader@email.com")
        assertThat(globalUserPermissions[0].permission.name).isEqualTo("reports.read")
        assertThat(globalUserPermissions[0].permission.scope).isInstanceOf(Scope.Global::class.java)

        assertThat(result.lastKey().username).isEqualTo("scoped.reader.name")
        val reportUserPermissions = result[result.lastKey()]!!
        assertThat(reportUserPermissions.count()).isEqualTo(1)
        assertThat(reportUserPermissions[0].userGroup).isEqualTo("scoped.reader@email.com")
        assertThat(reportUserPermissions[0].permission.name).isEqualTo("reports.read")
        assertThat(reportUserPermissions[0].permission.scope).isInstanceOf(Scope.Specific::class.java)
        assertThat(reportUserPermissions[0].permission.scope.value).isEqualTo("report:report1")
    }

    @Test
    fun `getReportReaders gets all readers with permissions from non-identity groups`()
    {
        insertReport("report1", "version1")
        insertReport("report2", "version2")

        insertUserGroup("global.readers")
        giveUserGroupPermission("global.readers", "reports.read", Scope.Global())

        insertUserGroup("report1.readers")
        giveUserGroupPermission("report1.readers", "reports.read", Scope.Specific("report", "report1"))

        insertUserGroup("report2.readers")
        giveUserGroupPermission("report2.readers", "reports.read", Scope.Specific("report", "report2"))

        insertUser("global@reader.com", "global reader")
        giveUserGroupMember("global.readers", "global@reader.com")

        insertUser("report1@reader.com", "report1 reader")
        giveUserGroupMember("report1.readers", "report1@reader.com")

        insertUser("report2@reader.com", "report2 reader")
        giveUserGroupMember("report2.readers", "report2@reader.com")

        insertUser("all.groups@reader.com", "all groups reader")
        giveUserGroupMember("global.readers", "all.groups@reader.com")
        giveUserGroupMember("report1.readers", "all.groups@reader.com")
        giveUserGroupMember("report2.readers", "all.groups@reader.com")
        //also give permission at user level
        giveUserGroupPermission("all.groups@reader.com", "reports.read", Scope.Global())

        val sut = OrderlyUserRepository()
        val result = sut.getReportReaders("report1").toSortedMap(compareBy { it.username })

        assertThat(result.count()).isEqualTo(3)

        assertThat(result.firstKey().username).isEqualTo("all groups reader")
        var permissions = result[result.firstKey()]!!
        assertThat(permissions.count()).isEqualTo(3)
        assertThat(permissions.any { it.userGroup == "global.readers" && it.permission.scope is Scope.Global })
        assertThat(permissions.any {
            it.userGroup == "report1.readers" && it.permission.scope is Scope.Specific
                    && it.permission.scope.value == "report:report1"
        })
        assertThat(permissions.any { it.userGroup == "all.groups@reader.com" && it.permission.scope is Scope.Global })

        result.remove(result.firstKey())
        assertThat(result.firstKey().username).isEqualTo("global reader")
        permissions = result[result.firstKey()]!!
        assertThat(permissions.count()).isEqualTo(1)
        assertThat(permissions[0].userGroup).isEqualTo("global.readers")
        assertThat(permissions[0].permission.scope).isInstanceOf(Scope.Global::class.java)

        result.remove(result.firstKey())
        assertThat(result.firstKey().username).isEqualTo("report1 reader")
        permissions = result[result.firstKey()]!!
        assertThat(permissions.count()).isEqualTo(1)
        assertThat(permissions[0].userGroup).isEqualTo("report1.readers")
        assertThat(permissions[0].permission.scope).isInstanceOf(Scope.Specific::class.java)
        assertThat(permissions[0].permission.scope.value).isEqualTo("report:report1")
    }

    @Test
    fun `gets global report reading groups`()
    {
        insertReport("report1", "version1")
        createGroup("Funder", ReifiedPermission("reports.read", Scope.Global()))
        createGroup("Science", ReifiedPermission("reports.read", Scope.Global()))
        createGroup("Tech", ReifiedPermission("reports.read", Scope.Specific("report", "report1")),
                ReifiedPermission("users.manage", Scope.Global()))

        addMembers("Funder", "funder.a@example.com", "funder.b@example.com")
        addMembers("Tech", "tech.user@example.com")

        val sut = OrderlyUserRepository()
        val result = sut.getGlobalReportReaderGroups()

        assertThat(result.count()).isEqualTo(1)
        assertThat(result[0].name).isEqualTo("Funder")
        assertThat(result[0].members.map { it.email })
                .containsExactlyElementsOf(listOf("funder.a@example.com", "funder.b@example.com"))
    }

    @Test
    fun `global report reading group members are ordered alphabetically`()
    {
        createGroup("Funder", ReifiedPermission("reports.read", Scope.Global()))
        addMembers("Funder", "c.user@example.com", "a.user@example.com", "b.user@example.com")

        val sut = OrderlyUserRepository()
        val result = sut.getGlobalReportReaderGroups()

        assertThat(result.count()).isEqualTo(1)
        assertThat(result[0].name).isEqualTo("Funder")
        assertThat(result[0].members.map { it.email })
                .containsExactlyElementsOf(
                        listOf("a.user@example.com",
                                "b.user@example.com",
                                "c.user@example.com"))
    }
}