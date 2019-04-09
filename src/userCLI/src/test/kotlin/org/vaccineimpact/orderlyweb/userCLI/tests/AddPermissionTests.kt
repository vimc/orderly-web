package org.vaccineimpact.orderlyweb.userCLI.tests

import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.userCLI.grantPermissions
import org.vaccineimpact.orderlyweb.userCLI.addUser

class AddPermissionTests : CleanDatabaseTests()
{
    @Test
    fun `addPermissionToGroup adds permission to group`()
    {
        insertReport("testreport", "v1")
        addUser(mapOf("<email>" to "test.user@email.com"))
        val result = grantPermissions(mapOf("<group>" to "test.user@email.com", "<permission>" to listOf("*/reports.read", "report:testreport/reports.review")))

        val expected = """Gave user group 'test.user@email.com' the permission '*/reports.read'
            |Gave user group 'test.user@email.com' the permission 'report:testreport/reports.review'
        """.trimMargin()

        assertThat(result).isEqualTo(expected)
        val permissions = OrderlyAuthorizationRepository().getPermissionsForUser("test.user@email.com")

        assertThat(permissions).hasSameElementsAs(
                listOf(ReifiedPermission("reports.read", Scope.Global()),
                        ReifiedPermission("reports.review", Scope.Specific("report", "testreport"))))
    }

    @Test
    fun `addPermissionToGroup does nothing if user group does not exist`()
    {
        assertThatThrownBy {
            grantPermissions(mapOf("<group>" to "test.user@email.com", "<permission>" to listOf("*/reports.read")))

        }.hasMessageContaining("Unknown user-group : 'test.user@email.com'")

        val permissions = OrderlyAuthorizationRepository().getPermissionsForUser("test.user@email.com")
        assertThat(permissions.count()).isEqualTo(0)
    }


    @Test
    fun `addPermissionToGroup does nothing if permission does not exist or is badly formatted`()
    {
        addUser(mapOf("<email>" to "test.user@email.com"))

        assertThatThrownBy {
            grantPermissions(mapOf("<group>" to "test.user@email.com", "<permission>" to listOf("*/permission.read")))
        }.hasMessageContaining("Unknown permission : 'permission.read'")

        assertThatThrownBy {
            grantPermissions(mapOf("<group>" to "test.user@email.com", "<permission>" to listOf("badlyformatted/reports.read")))
        }.hasMessageContaining("Unable to parse 'badlyformatted/reports.read' as a ReifiedPermission")

        val permissions = OrderlyAuthorizationRepository().getPermissionsForUser("test.user@email.com")

        assertThat(permissions.count()).isEqualTo(0)
    }
}