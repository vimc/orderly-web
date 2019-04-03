package org.vaccineimpact.orderlyweb.tests.database_tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.security.authorization.AuthorizationRepository
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

        val sut = AuthorizationRepository()
        val result = sut.generate(mock(), mock { on { it.id } doReturn "user@email.com" })
        assertThat(result.orderlyWebPermissions).isEmpty()
    }

    @Test
    fun `can get permissions for user`()
    {
        JooqContext().use {
            insertUser("user@email.com", "user.name")

            giveUserGroupPermission("user@email.com", "report.read", Scope.Global(), addPermission = true)
            giveUserGroupPermission("user@email.com", "report.read", Scope.Specific("report", "r1"), addPermission = false)

            insertReport("r1", "r1v1")
            insertReport("r2", "r2v1")
        }

        val sut = AuthorizationRepository()
        val profile = CommonProfile()
        profile.setId("user@email.com")

        val result = sut.generate(mock(), profile)

        assertThat(result.orderlyWebPermissions)
                .hasSameElementsAs(listOf(ReifiedPermission("report.read", Scope.Global()),
                        ReifiedPermission("report.read", Scope.Specific("report", "r1")),
                        ReifiedPermission("report.read", Scope.Specific("report", "r2"))))
    }

}