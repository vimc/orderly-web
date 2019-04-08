package org.vaccineimpact.orderlyweb.userCLI.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_USER
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.userCLI.addPermissionToGroup
import org.vaccineimpact.orderlyweb.userCLI.addUser

class AddPermissionTests : CleanDatabaseTests()
{
    @Test
    fun `addPermissionToGroup adds permission to group`()
    {
        insertReport("testreport", "v1")
        addUser(mapOf("<email>" to "test.user@email.com"))
        addPermissionToGroup(mapOf("<group>" to "test.user@email.com", "<permission>" to "*/reports.read"))
        addPermissionToGroup(mapOf("<group>" to "test.user@email.com", "<permission>" to "report:testreport/reports.review"))

        val permissions = OrderlyAuthorizationRepository().getPermissionsForUser("test.user@email.com")

        Assertions.assertThat(permissions).hasSameElementsAs(
                listOf(ReifiedPermission("reports.read", Scope.Global()),
                        ReifiedPermission("reports.review", Scope.Specific("report", "testreport"))))
    }

    @Test
    fun `addPermissionToGroup does nothing if user group does not exist`()
    {
        addPermissionToGroup(mapOf("<group>" to "test.user@email.com", "<permission>" to "*/reports.read"))
        val permissions = OrderlyAuthorizationRepository().getPermissionsForUser("test.user@email.com")

        Assertions.assertThat(permissions.count()).isEqualTo(0)
    }


    @Test
    fun `addPermissionToGroup does nothing if permission does not exist or is badly formatted`()
    {
        addUser(mapOf("<email>" to "test.user@email.com"))

        addPermissionToGroup(mapOf("<group>" to "test.user@email.com", "<permission>" to "*/permission.read"))
        addPermissionToGroup(mapOf("<group>" to "test.user@email.com", "<permission>" to "badlyformatted/reports.read"))

        val permissions = OrderlyAuthorizationRepository().getPermissionsForUser("test.user@email.com")

        Assertions.assertThat(permissions.count()).isEqualTo(0)
    }
}