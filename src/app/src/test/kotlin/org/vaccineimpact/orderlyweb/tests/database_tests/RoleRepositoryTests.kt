package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyRoleRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.addMembers
import org.vaccineimpact.orderlyweb.tests.createGroup
import org.vaccineimpact.orderlyweb.tests.giveUserGroupPermission
import org.vaccineimpact.orderlyweb.tests.insertUser

class RoleRepositoryTests : CleanDatabaseTests()
{
    @Test
    fun `gets global report reading roles`()
    {
        insertReport("report1", "version1")
        createGroup("Funder", ReifiedPermission("reports.read", Scope.Global()))
        createGroup("Science", ReifiedPermission("reports.read", Scope.Global()))

        addMembers("Funder", "funder.a@example.com", "funder.b@example.com")
        addMembers("Science", "science.user@example.com")

        val sut = OrderlyRoleRepository()
        val result = sut.getGlobalReportReaderRoles()

        assertThat(result.count()).isEqualTo(2)
        assertThat(result[0].name).isEqualTo("Funder")
        assertThat(result[0].members.map { it.email })
                .containsExactlyElementsOf(listOf("funder.a@example.com", "funder.b@example.com"))
        assertThat(result[0].permissions).containsExactlyElementsOf(listOf(ReifiedPermission("reports.read", Scope.Global())))

        assertThat(result[1].name).isEqualTo("Science")
        assertThat(result[1].members.map { it.email })
                .containsExactlyElementsOf(listOf("science.user@example.com"))
        assertThat(result[1].permissions).containsExactlyElementsOf(listOf(ReifiedPermission("reports.read", Scope.Global())))
    }

    @Test
    fun `gets all roles`()
    {
        createGroup("Admin", ReifiedPermission("users.manage", Scope.Global()))
        createGroup("Science", ReifiedPermission("reports.read", Scope.Global()))
        createGroup("Reviewer", ReifiedPermission("reports.review", Scope.Global()))
        addMembers("Admin", "admin.a@example.com", "admin.b@example.com")
        addMembers("Science", "science.user@example.com")
        insertUser("notInARole@example.com", "Not Role")

        val sut = OrderlyRoleRepository()
        val result = sut.getAllRoles().sortedBy { r -> r.name }

        assertThat(result.count()).isEqualTo(3)
        assertThat(result[0].name).isEqualTo("Admin")
        assertThat(result[0].members.map { it.email })
                .containsExactlyElementsOf(listOf("admin.a@example.com", "admin.b@example.com"))

        assertThat(result[1].name).isEqualTo("Reviewer")
        assertThat(result[1].members.count()).isEqualTo(0)

        assertThat(result[2].name).isEqualTo("Science")
        assertThat(result[2].members.map { it.email })
                .containsExactlyElementsOf(listOf("science.user@example.com"))
    }

    @Test
    fun `getGlobalReportReaderRoles does not return specific report reading groups`()
    {
        insertReport("report1", "version1")
        createGroup("Tech", ReifiedPermission("reports.read", Scope.Specific("report", "report1")))
        addMembers("Tech", "tech.user@example.com")

        val sut = OrderlyRoleRepository()
        val result = sut.getGlobalReportReaderRoles()

        assertThat(result.count()).isEqualTo(0)
    }

    @Test
    fun `getGlobalReportReaderRoles does not return non report reading groups`()
    {
        createGroup("Admin", ReifiedPermission("users.manage", Scope.Global()))
        addMembers("Admin", "admin.user@example.com")

        val sut = OrderlyRoleRepository()
        val result = sut.getGlobalReportReaderRoles()

        assertThat(result.count()).isEqualTo(0)
    }

    @Test
    fun `getGlobalReportReaderRoles returns groups with no members`()
    {
        createGroup("Admin", ReifiedPermission("reports.read", Scope.Global()))

        val sut = OrderlyRoleRepository()
        val result = sut.getGlobalReportReaderRoles()

        assertThat(result.count()).isEqualTo(1)
    }

    @Test
    fun `getGlobalReportReaderRoles does not return identity groups`()
    {
        insertUser("test.user@example.com", "Test User")
        giveUserGroupPermission("test.user@example.com", "reports.read", Scope.Global())

        val sut = OrderlyRoleRepository()
        val result = sut.getGlobalReportReaderRoles()

        assertThat(result.count()).isEqualTo(0)
    }

    @Test
    fun `gets scoped report reading roles`()
    {
        insertReport("r1", "version1")
        createGroup("Funder", ReifiedPermission("reports.read", Scope.Specific("report", "r1")))
        createGroup("Science", ReifiedPermission("reports.read", Scope.Specific("report", "r1")))

        addMembers("Funder", "funder.a@example.com", "funder.b@example.com")
        addMembers("Science", "science.user@example.com")

        val sut = OrderlyRoleRepository()
        val result = sut.getScopedReportReaderRoles("r1")

        assertThat(result.count()).isEqualTo(2)
        assertThat(result[0].name).isEqualTo("Funder")
        assertThat(result[0].members.map { it.email })
                .containsExactlyElementsOf(listOf("funder.a@example.com", "funder.b@example.com"))
        assertThat(result[0].permissions)
                .containsExactlyElementsOf(listOf(ReifiedPermission("reports.read", Scope.Specific("report", "r1"))))

        assertThat(result[1].name).isEqualTo("Science")
        assertThat(result[1].members.map { it.email })
                .containsExactlyElementsOf(listOf("science.user@example.com"))
        assertThat(result[1].permissions)
                .containsExactlyElementsOf(listOf(ReifiedPermission("reports.read", Scope.Specific("report", "r1"))))
    }

    @Test
    fun `getScopedReportReaderRoless does not return global report reading groups`()
    {
        insertReport("r1", "version1")
        createGroup("Tech", ReifiedPermission("reports.read", Scope.Global()))
        addMembers("Tech", "tech.user@example.com")

        val sut = OrderlyRoleRepository()
        val result = sut.getScopedReportReaderRoles("r1")

        assertThat(result.count()).isEqualTo(0)
    }

    @Test
    fun `getScopedReportReaderRoles does not return wrongly scoped report reading groups`()
    {
        insertReport("r2", "v1")
        createGroup("Admin", ReifiedPermission("users.manage", Scope.Specific("report", "r2")))
        addMembers("Admin", "admin.user@example.com")

        val sut = OrderlyRoleRepository()
        val result = sut.getScopedReportReaderRoles("r1")

        assertThat(result.count()).isEqualTo(0)
    }

    @Test
    fun `getScopedReportReaderRoles does not return non report reading groups`()
    {
        createGroup("Admin", ReifiedPermission("users.manage", Scope.Global()))
        addMembers("Admin", "admin.user@example.com")

        val sut = OrderlyRoleRepository()
        val result = sut.getScopedReportReaderRoles("r1")

        assertThat(result.count()).isEqualTo(0)
    }

    @Test
    fun `getScopedReportReaderRoles returns groups with no members`()
    {
        insertReport("r1", "v1")
        createGroup("Admin", ReifiedPermission("reports.read", Scope.Specific("report", "r1")))

        val sut = OrderlyRoleRepository()
        val result = sut.getScopedReportReaderRoles("r1")

        assertThat(result.count()).isEqualTo(1)
    }

    @Test
    fun `getScopedReportReaderRoles does not return identity groups`()
    {
        insertReport("r1", "v1")
        insertUser("test.user@example.com", "Test User")
        giveUserGroupPermission("test.user@example.com", "reports.read", Scope.Specific("report", "r1"))

        val sut = OrderlyRoleRepository()
        val result = sut.getScopedReportReaderRoles("r1")

        assertThat(result.count()).isEqualTo(0)
    }

    @Test
    fun `getAllRoleNames gets non-id group names`()
    {

        insertUser("test.user@example.com", "Test User")

        createGroup("Admin")
        addMembers("Admin", "test.user@example.com")

        createGroup("Funder")

        val sut = OrderlyRoleRepository()
        val result = sut.getAllRoleNames()

        assertThat(result).containsExactly("Admin", "Funder")
    }
}
