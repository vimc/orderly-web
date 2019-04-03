package org.vaccineimpact.orderlyweb.tests.database_tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.security.authorization.orderlyWebPermissions
import org.vaccineimpact.orderlyweb.tests.giveUserGroupPermission
import org.vaccineimpact.orderlyweb.tests.insertReport
import org.vaccineimpact.orderlyweb.tests.insertUser

class OrderlyWebAuthorizationRepositoryTests : CleanDatabaseTests()
{
    @Test
    fun `can get empty permission set for user`()
    {
        JooqContext().use {
            insertUser("user@email.com", "user.name")
        }

        val sut = OrderlyAuthorizationRepository()
        val result = sut.generate(mock(), mock { on { it.id } doReturn "user@email.com" })
        assertThat(result.orderlyWebPermissions).isEmpty()
    }

    @Test
    fun `can get permissions for user`()
    {
        JooqContext().use {
            insertUser("user@email.com", "user.name")

            insertReport("r1", "r1v1")
            insertReport("r2", "r2v1")
            giveUserGroupPermission("user@email.com", "reports.read", Scope.Global(), addPermission = true)
            giveUserGroupPermission("user@email.com", "reports.read", Scope.Specific("report", "r1"),
                    addPermission = false)
            giveUserGroupPermission("user@email.com", "reports.read", Scope.Specific("report", "r2"),
                    addPermission = false)

        }

        val sut = OrderlyAuthorizationRepository()
        val profile = CommonProfile()
        profile.setId("user@email.com")

        val result = sut.generate(mock(), profile)

        assertThat(result.orderlyWebPermissions)
                .hasSameElementsAs(listOf(ReifiedPermission("reports.read", Scope.Global()),
                        ReifiedPermission("reports.read", Scope.Specific("report", "r1")),
                        ReifiedPermission("reports.read", Scope.Specific("report", "r2"))))
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

        val profile = CommonProfile()
        profile.setId("user@email.com")

        val result = sut.generate(mock(), profile)

        assertThat(result.orderlyWebPermissions)
                .hasSameElementsAs(listOf(ReifiedPermission("reports.read", Scope.Global()),
                        ReifiedPermission("reports.read", Scope.Specific("report", "fakereport")),
                        ReifiedPermission("reports.read", Scope.Specific("version", "v1"))))
    }

    @Test
    fun `ensureUserGroupHasPermission does nothing if user already has permission`()
    {
        JooqContext().use {
            insertUser("user@email.com", "user.name")
            giveUserGroupPermission("user@email.com", "reports.read", Scope.Global(),
                    addPermission = true)
        }

        val sut = OrderlyAuthorizationRepository()

        sut.ensureUserGroupHasPermission("user@email.com",
                ReifiedPermission("reports.read", Scope.Global()))

        val profile = CommonProfile()
        profile.setId("user@email.com")

        val result = sut.generate(mock(), profile)

        assertThat(result.orderlyWebPermissions)
                .hasSameElementsAs(listOf(ReifiedPermission("reports.read", Scope.Global())))
    }

}