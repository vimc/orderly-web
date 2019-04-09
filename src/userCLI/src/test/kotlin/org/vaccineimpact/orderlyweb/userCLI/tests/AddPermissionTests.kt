package org.vaccineimpact.orderlyweb.userCLI.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.userCLI.addPermissionsToGroup
import org.vaccineimpact.orderlyweb.userCLI.addUser

class AddPermissionTests : CleanDatabaseTests()
{
    @Test
    fun `addPermissionsToGroup adds permissions to group`()
    {
        insertReport("testreport", "v1")
        addUser(mapOf("<email>" to "test.user@email.com"))
        addPermissionsToGroup(mapOf("<group>" to "test.user@email.com", "<permission>" to
                listOf("*/reports.read", "report:testreport/reports.review")))

        val permissions = OrderlyAuthorizationRepository().getPermissionsForUser("test.user@email.com")

        Assertions.assertThat(permissions).hasSameElementsAs(
                listOf(ReifiedPermission("reports.read", Scope.Global()),
                        ReifiedPermission("reports.review", Scope.Specific("report", "testreport"))))
    }

    @Test
    fun `addPermissionsToGroup does nothing if user group does not exist`()
    {
        addPermissionsToGroup(mapOf("<group>" to "test.user@email.com", "<permission>" to listOf("*/reports.read")))
        val permissions = OrderlyAuthorizationRepository().getPermissionsForUser("test.user@email.com")

        Assertions.assertThat(permissions.count()).isEqualTo(0)
    }


    @Test
    fun `addPermissionsToGroup does nothing if permission does not exist or is badly formatted`()
    {
        addUser(mapOf("<email>" to "test.user@email.com"))

        addPermissionsToGroup(mapOf("<group>" to "test.user@email.com", "<permission>" to listOf("*/permission.read")))
        addPermissionsToGroup(mapOf("<group>" to "test.user@email.com", "<permission>" to listOf("badlyformatted/reports.read")))

        val permissions = OrderlyAuthorizationRepository().getPermissionsForUser("test.user@email.com")

        Assertions.assertThat(permissions.count()).isEqualTo(0)
    }
}