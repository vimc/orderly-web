package org.vaccineimpact.orderlyweb.tests.security.authorization

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.db.repositories.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator
import org.vaccineimpact.orderlyweb.security.authorization.orderlyWebPermissions
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.tests.insertUser

class OrderlyWebAuthorizationRepositoryTests : CleanDatabaseTests()
{
    @Test
    fun `gets permission set for user`()
    {
        JooqContext().use {
            insertUser("user@email.com", "user.name")
        }

        val fakePermissions = listOf(ReifiedPermission("reports.read", Scope.Global()))

        val mockRepo = mock<AuthorizationRepository> {
            on { getPermissionsForUser("user@email.com") } doReturn fakePermissions.map { it.toString() }
        }

        val sut = OrderlyAuthorizationGenerator(mockRepo)
        val result = sut.generate(mock(), mock(), CommonProfile().apply { id = "user@email.com" })
        assertThat((result.get() as CommonProfile).orderlyWebPermissions)
                .hasSameElementsAs(PermissionSet(fakePermissions))
    }
}