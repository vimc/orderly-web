package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jooq.exception.DataAccessException
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.errors.DuplicateKeyError
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.tests.giveUserGroupPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.insertUser
import org.vaccineimpact.orderlyweb.tests.insertUserGroup
import org.vaccineimpact.orderlyweb.tests.giveUserGroupMember
import org.vaccineimpact.orderlyweb.tests.giveUserGroupPermission

class OrderlyWebAuthorizationRepositoryTests : CleanDatabaseTests()
{
    @Test
    fun `can get empty permission set for user`()
    {
        JooqContext().use {
            insertUser("user@email.com", "user.name")
        }

        val sut = OrderlyAuthorizationRepository()
        val result = sut.getPermissionsForUser("user@email.com")
        assertThat(result).isEmpty()
    }

    @Test
    fun `can get permissions for user`()
    {
        JooqContext().use {
            insertUser("user@email.com", "user.name")

            insertReport("r1", "r1v1")
            insertReport("r2", "r2v1")

            giveUserGroupPermission("user@email.com", "reports.read", Scope.Global())
            giveUserGroupPermission("user@email.com", "reports.read", Scope.Specific("report", "r1"))
            giveUserGroupPermission("user@email.com", "reports.read", Scope.Specific("report", "r2"))

        }

        val sut = OrderlyAuthorizationRepository()

        val result = sut.getPermissionsForUser("user@email.com")

        assertThat(result)
                .hasSameElementsAs(listOf(ReifiedPermission("reports.read", Scope.Global()),
                        ReifiedPermission("reports.read", Scope.Specific("report", "r1")),
                        ReifiedPermission("reports.read", Scope.Specific("report", "r2"))))
    }

    @Test
    fun `can add user group`()
    {
        val sut = OrderlyAuthorizationRepository()
        sut.createUserGroup("testgroup")

        val groups = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER_GROUP).fetch()
        }

        Assertions.assertThat(groups.count()).isEqualTo(1)
        Assertions.assertThat(groups.first()[ORDERLYWEB_USER_GROUP.ID]).isEqualTo("testgroup")
    }

    @Test
    fun `cannot add duplicate user groups`()
    {
        val sut = OrderlyAuthorizationRepository()
        sut.createUserGroup("testgroup")

        assertThatThrownBy { sut.createUserGroup("testgroup") }
                .isInstanceOf(DuplicateKeyError::class.java)
                .hasMessageContaining("An object with the id 'testgroup' already exists")
    }

    @Test
    fun `can add permissions to user group`()
    {
        JooqContext().use {
            insertUser("user@email.com", "user.name")
            insertReport("fakereport", "v1")
        }

        val sut = OrderlyAuthorizationRepository()

        sut.ensureUserGroupHasPermission("user@email.com",
                ReifiedPermission("reports.read", Scope.Global()))

        sut.ensureUserGroupHasPermission("user@email.com",
                ReifiedPermission("reports.read", Scope.Specific("report", "fakereport")))

        sut.ensureUserGroupHasPermission("user@email.com",
                ReifiedPermission("reports.read", Scope.Specific("version", "v1")))

        val result = sut.getPermissionsForUser("user@email.com")

        assertThat(result)
                .hasSameElementsAs(listOf(ReifiedPermission("reports.read", Scope.Global()),
                        ReifiedPermission("reports.read", Scope.Specific("report", "fakereport")),
                        ReifiedPermission("reports.read", Scope.Specific("version", "v1"))))
    }

    @Test
    fun `ensureUserGroupHasPermission does nothing if user already has permission`()
    {
        JooqContext().use {
            insertUser("user@email.com", "user.name")
            giveUserGroupPermission("user@email.com", "reports.read", Scope.Global())
        }

        val sut = OrderlyAuthorizationRepository()

        sut.ensureUserGroupHasPermission("user@email.com",
                ReifiedPermission("reports.read", Scope.Global()))

        val result = sut.getPermissionsForUser("user@email.com")

        assertThat(result)
                .hasSameElementsAs(listOf(ReifiedPermission("reports.read", Scope.Global())))
    }

    @Test
    fun `ensureUserGroupHasPermission throws UnknownObjectError if permission does not exist`()
    {
        JooqContext().use {
            insertUser("user@email.com", "user.name")
        }

        val sut = OrderlyAuthorizationRepository()

        assertThatThrownBy {
            sut.ensureUserGroupHasPermission("user@email.com",
                    ReifiedPermission("nonexistent.permission", Scope.Global()))
        }.isInstanceOf(UnknownObjectError::class.java)
                .hasMessageContaining("Unknown permission : 'nonexistent.permission'")
    }

    @Test
    fun `ensureUserGroupHasPermission throws UnknownObjectError if group does not exist`()
    {
        val sut = OrderlyAuthorizationRepository()

        assertThatThrownBy {
            sut.ensureUserGroupHasPermission("nonsense",
                    ReifiedPermission("reports.read", Scope.Global()))
        }.isInstanceOf(UnknownObjectError::class.java)
                .hasMessageContaining("Unknown user-group : 'nonsense'")
    }

    @Test
    fun `can remove permissions from user group`()
    {
        JooqContext().use {
            insertUser("user@email.com", "user.name")
            insertReport("fakereport", "v1")
            insertReport("fakereport2", "v2")
        }

        val sut = OrderlyAuthorizationRepository()

        //Add permissions - as tested above
        sut.ensureUserGroupHasPermission("user@email.com",
                ReifiedPermission("reports.review", Scope.Global()))

        sut.ensureUserGroupHasPermission("user@email.com",
                ReifiedPermission("reports.read", Scope.Global()))

        sut.ensureUserGroupHasPermission("user@email.com",
                ReifiedPermission("reports.read", Scope.Specific("report", "fakereport")))

        sut.ensureUserGroupHasPermission("user@email.com",
                ReifiedPermission("reports.read", Scope.Specific("version", "v1")))

        sut.ensureUserGroupHasPermission("user@email.com",
                ReifiedPermission("reports.read", Scope.Specific("version", "v2")))

        //..then remove some
        sut.ensureUserGroupDoesNotHavePermission("user@email.com",
                ReifiedPermission("reports.review", Scope.Global()))

        sut.ensureUserGroupDoesNotHavePermission("user@email.com",
                ReifiedPermission("reports.read", Scope.Specific("report", "fakereport")))

        sut.ensureUserGroupDoesNotHavePermission("user@email.com",
                ReifiedPermission("reports.read", Scope.Specific("version", "v1")))


        val result = sut.getPermissionsForUser("user@email.com")

        assertThat(result)
                .hasSameElementsAs(listOf(ReifiedPermission("reports.read", Scope.Global()),
                        ReifiedPermission("reports.read", Scope.Specific("version", "v2"))))
    }

    @Test
    fun `ensureUserGroupDoesNotHavePermission does nothing if user does not already have permission`()
    {
        JooqContext().use {
            insertUser("user@email.com", "user.name")
            giveUserGroupPermission("user@email.com", "reports.read", Scope.Global())
        }

        val sut = OrderlyAuthorizationRepository()

        sut.ensureUserGroupHasPermission("user@email.com",
                ReifiedPermission("reports.read", Scope.Global()))

        sut.ensureUserGroupDoesNotHavePermission("user@email.com",
                ReifiedPermission("reports.review", Scope.Global()))

        val result = sut.getPermissionsForUser("user@email.com")

        assertThat(result)
                .hasSameElementsAs(listOf(ReifiedPermission("reports.read", Scope.Global())))
    }

    @Test
    fun `ensureUserGroupDoesNotHavePermission throws UnknownObjectError if permission does not exist`()
    {
        JooqContext().use {
            insertUser("user@email.com", "user.name")
        }

        val sut = OrderlyAuthorizationRepository()

        assertThatThrownBy {
            sut.ensureUserGroupDoesNotHavePermission("user@email.com",
                    ReifiedPermission("nonexistent.permission", Scope.Global()))
        }.isInstanceOf(UnknownObjectError::class.java)
                .hasMessageContaining("Unknown permission : 'nonexistent.permission'")
    }

    @Test
    fun `ensureUserGroupDoesNotHavePermission throws UnknownObjectError if group does not exist`()
    {
        val sut = OrderlyAuthorizationRepository()

        assertThatThrownBy {
            sut.ensureUserGroupDoesNotHavePermission("nonsense",
                    ReifiedPermission("reports.read", Scope.Global()))
        }.isInstanceOf(UnknownObjectError::class.java)
                .hasMessageContaining("Unknown user-group : 'nonsense'")
    }

    @Test
    fun `can add user to group`()
    {
        val sut = OrderlyAuthorizationRepository()
        sut.createUserGroup("somegroup")
        JooqContext().use {
            it.dsl.insertInto(ORDERLYWEB_USER)
                    .set(ORDERLYWEB_USER.EMAIL, "user@email.com")
                    .set(ORDERLYWEB_USER.USER_SOURCE, "GitHub")
                    .set(ORDERLYWEB_USER.USERNAME, "user.name")
                    .execute()
        }
        sut.ensureGroupHasMember("somegroup", "user@email.com")

        val user = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER_GROUP_USER)
                    .where(ORDERLYWEB_USER_GROUP_USER.USER_GROUP.eq("somegroup"))
                    .single()
        }

        assertThat(user[ORDERLYWEB_USER_GROUP_USER.EMAIL]).isEqualTo("user@email.com")
    }

    @Test
    fun `ensureGroupHasMember does nothing if user already in group`()
    {
        val sut = OrderlyAuthorizationRepository()
        sut.createUserGroup("somegroup")
        JooqContext().use {
            it.dsl.insertInto(ORDERLYWEB_USER)
                    .set(ORDERLYWEB_USER.EMAIL, "user@email.com")
                    .set(ORDERLYWEB_USER.USER_SOURCE, "GitHub")
                    .set(ORDERLYWEB_USER.USERNAME, "user.name")
                    .execute()
        }

        sut.ensureGroupHasMember("somegroup", "user@email.com")
        sut.ensureGroupHasMember("somegroup", "user@email.com")

        val user = JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER_GROUP_USER)
                    .where(ORDERLYWEB_USER_GROUP_USER.USER_GROUP.eq("somegroup"))
                    .single()
        }

        assertThat(user[ORDERLYWEB_USER_GROUP_USER.EMAIL]).isEqualTo("user@email.com")
    }

    @Test
    fun `ensureGroupHasMember throws UnknownObjectError if group does not exist`()
    {
        val sut = OrderlyAuthorizationRepository()

        assertThatThrownBy {
            sut.ensureGroupHasMember("nonsense", "user@email.com")
        }.isInstanceOf(UnknownObjectError::class.java)
                .hasMessageContaining("Unknown user-group : 'nonsense'")
    }

    @Test
    fun `ensureGroupHasMember throws UnknownObjectError if user does not exist`()
    {
        val sut = OrderlyAuthorizationRepository()
        sut.createUserGroup("testgroup")
        assertThatThrownBy {
            sut.ensureGroupHasMember("testgroup", "user@email.com")
        }.isInstanceOf(UnknownObjectError::class.java)
                .hasMessageContaining("Unknown user : 'user@email.com'")
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

        val sut = OrderlyAuthorizationRepository()
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
        giveUserGroupMember("report2.readers", "report2.reader.com")

        insertUser("all.groups@reader.com", "all groups reader")
        giveUserGroupMember("global.readers", "all.groups@reader.com")
        giveUserGroupMember("report1.readers", "all.groups@reader.com")
        giveUserGroupMember("report2.readers", "all.groups@reader.com")
        //also give permission at user level
        giveUserGroupPermission("all.groups@reader.com", "reports.read", Scope.Global())

        val sut = OrderlyAuthorizationRepository()
        val result = sut.getReportReaders("report1").toSortedMap(compareBy { it.username })

        assertThat(result.count()).isEqualTo(3)

        assertThat(result.firstKey().username).isEqualTo("all groups reader")
        var permissions = result[result.firstKey()]!!
        assertThat(permissions.count()).isEqualTo(3)
        assertThat(permissions.any{ it.userGroup == "global.readers" && it.permission.scope is Scope.Global })
        assertThat(permissions.any{ it.userGroup == "report1.readers" && it.permission.scope is Scope.Specific
                                        && it.permission.scope.value == "report:report1"})
        assertThat(permissions.any{ it.userGroup == "all.groups@reader.com" && it.permission.scope is Scope.Global })

        result.remove(result.firstKey())
        assertThat(result.firstKey().username).isEqualTo("global reader")
        permissions = result[result.firstKey()]!!
        assertThat(permissions.count()).isEqualTo(1)
        assertThat(permissions[0].userGroup).isEqualTo("global.readers")
        assertThat(permissions[0].permission).isInstanceOf(Scope.Global::class.java)

        result.remove(result.firstKey())
        assertThat(result.firstKey().username).isEqualTo("report1 reader")
        permissions = result[result.firstKey()]!!
        assertThat(permissions.count()).isEqualTo(1)
        assertThat(permissions[0].userGroup).isEqualTo("report1.readers")
        assertThat(permissions[0].permission.scope).isInstanceOf(Scope.Specific::class.java)
        assertThat(permissions[0].permission.scope.value).isEqualTo("report:report1")
    }

}