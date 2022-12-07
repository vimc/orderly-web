package org.vaccineimpact.orderlyweb.userCLI.tests

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.userCLI.addUsers
import org.vaccineimpact.orderlyweb.userCLI.grantPermissions

class AddPermissionTests : CleanDatabaseTests()
{
    @Test
    fun `addPermissionsToGroup adds permissions to group`()
    {
        insertReport("testreport", "v1")
        addUsers(mapOf("<email>" to listOf("[test.user@email.com]")))
        val result = grantPermissions(mapOf("<group>" to "[test.user@email.com]", "<permission>" to listOf("[*/reports.read]", "[report:testreport/reports.review]")))

        val expected = """Gave user group 'test.user@email.com' the permission '*/reports.read'
            |Gave user group 'test.user@email.com' the permission 'report:testreport/reports.review'
        """.trimMargin()

        assertThat(result).isEqualTo(expected)

        val permissions = OrderlyAuthorizationRepository().getPermissionsForUser("test.user@email.com")

        assertThat(permissions).hasSameElementsAs(
                listOf(ReifiedPermission("reports.read", Scope.Global()).toString(),
                        ReifiedPermission("reports.review", Scope.Specific("report", "testreport")).toString()))

    }

    @Test
    fun `addPermissionsToGroup does nothing if user group does not exist`()
    {
        assertThatThrownBy {
            grantPermissions(mapOf("<group>" to "[test.user@email.com]", "<permission>" to listOf("[*/reports.read]")))

        }.hasMessageContaining("Unknown user-group : 'test.user@email.com'")

        val permissions = OrderlyAuthorizationRepository().getPermissionsForUser("test.user@email.com")
        assertThat(permissions.count()).isEqualTo(0)
    }

    @Test
    fun `addPermissionsToGroup does nothing if permission does not exist or is badly formatted`()
    {
        addUsers(mapOf("<email>" to listOf("[test.user@email.com]")))

        assertThatThrownBy {
            grantPermissions(mapOf("<group>" to "[test.user@email.com]", "<permission>" to listOf("[*/permission.read]")))
        }.hasMessageContaining("Unknown permission : 'permission.read'")

        assertThatThrownBy {
            grantPermissions(mapOf("<group>" to "[test.user@email.com]", "<permission>" to listOf("[badlyformatted/reports.read]")))
        }.hasMessageContaining("Unable to parse 'badlyformatted/reports.read' as a ReifiedPermission")

        val permissions = OrderlyAuthorizationRepository().getPermissionsForUser("test.user@email.com")

        assertThat(permissions.count()).isEqualTo(0)
    }
}