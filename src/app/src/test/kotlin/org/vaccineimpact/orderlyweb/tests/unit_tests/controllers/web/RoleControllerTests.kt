package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.internal.verification.Times
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.RoleController
import org.vaccineimpact.orderlyweb.db.repositories.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.repositories.RoleRepository
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.errors.InvalidOperationError
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.models.permissions.Role
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.viewmodels.RoleViewModel

class RoleControllerTests : TeamcityTests()
{
    private val testPermissions = listOf(ReifiedPermission("reports.read", Scope.Global()),
            ReifiedPermission("reports.review", Scope.Specific("report", "r1")))

    private val singleRoleFromRepo = listOf(Role("Funders",
            listOf(User("test.user", "Test User", "test@example.com"),
                    User("unknown", "unknown", "funder@example.com"),
                    User("funder.user", "unknown", "another@example.com")
            ), testPermissions))

    private val multipleRolesFromRepo = listOf(
        Role("Science", listOf(), listOf()),
        Role("Funders", listOf(), listOf()),
        Role("Tech", listOf(), listOf())
    )

    private val roleForAlphabeticFromRepo = listOf(Role("Science", listOf(
            User("c.user", "C User", "testc@example.com"),
            User("a.user", "A User", "test@example.com"),
            User("b.user", "B User", "testb@example.com")
        ), listOf()))

    private fun assertExpectedSingleRoleViewModel(result: List<RoleViewModel>)
    {
        assertThat(result.count()).isEqualTo(1)
        assertThat(result[0].name).isEqualTo("Funders")

        val members = result[0].members
        assertThat(members[0].displayName).isEqualTo("Test User")
        assertThat(members[1].displayName).isEqualTo("funder.user")
        assertThat(members[2].displayName).isEqualTo("funder@example.com")
        assertThat(members[0].email).isEqualTo("test@example.com")
        assertThat(members[1].email).isEqualTo("another@example.com")
        assertThat(members[2].email).isEqualTo("funder@example.com")

        val perms = result[0].permissions
        assertThat(perms[0].name).isEqualTo("reports.read")
        assertThat(perms[0].scopeId).isEqualTo("")
        assertThat(perms[0].scopePrefix).isNull()
        assertThat(perms[1].name).isEqualTo("reports.review")
        assertThat(perms[1].scopeId).isEqualTo("r1")
        assertThat(perms[1].scopePrefix).isEqualTo("report")
    }

    private fun assertExpectedMultipleRoleViewModels(result: List<RoleViewModel>)
    {
        assertThat(result[0].name).isEqualTo("Funders")
        assertThat(result[1].name).isEqualTo("Science")
        assertThat(result[2].name).isEqualTo("Tech")
    }

    private fun assertExpectedAlphabeticRoleMembers(result: List<RoleViewModel>)
    {
        val members = result[0].members
        assertThat(members.map { it.username }).containsExactly("a.user", "b.user", "c.user")
    }

    @Test
    fun `getGlobalReportReaderGroups builds user group view models`()
    {
        val repo = mock<RoleRepository> {
            on { getGlobalReportReaderRoles() } doReturn singleRoleFromRepo
        }

        val sut = RoleController(mock(), repo, mock())
        val result = sut.getGlobalReportReaders()
        assertExpectedSingleRoleViewModel(result)
    }

    @Test
    fun `getAllRoles builds user group view models`()
    {
        val repo = mock<RoleRepository> {
            on { getAllRoles() } doReturn singleRoleFromRepo
        }

        val sut = RoleController(mock(), repo, mock())
        val result = sut.getAll()
        assertExpectedSingleRoleViewModel(result)
    }

    @Test
    fun `getGlobalReportReaderGroups orders user group view models alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getGlobalReportReaderRoles() } doReturn multipleRolesFromRepo
        }

        val sut = RoleController(mock(), repo, mock())
        val result = sut.getGlobalReportReaders()
        assertExpectedMultipleRoleViewModels(result)
    }

    @Test
    fun `getAllRoles orders user group view models alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getAllRoles() } doReturn multipleRolesFromRepo
        }

        val sut = RoleController(mock(), repo, mock())
        val result = sut.getAll()
        assertExpectedMultipleRoleViewModels(result)
    }

    @Test
    fun `getGlobalReportReaderGroups orders user group view model members alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getGlobalReportReaderRoles() } doReturn roleForAlphabeticFromRepo
        }

        val sut = RoleController(mock(), repo, mock())
        val result = sut.getGlobalReportReaders()
        assertExpectedAlphabeticRoleMembers(result)
    }

    @Test
    fun `getAllRoles orders user group view model members alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getAllRoles() } doReturn roleForAlphabeticFromRepo
        }

        val sut = RoleController(mock(), repo, mock())
        val result = sut.getAll()
        assertExpectedAlphabeticRoleMembers(result)
    }

    private val actionContextWithReport = mock<ActionContext> {
        on { this.params(":report") } doReturn "r1"
    }

    @Test
    fun `getScopedReportReaders builds user group view models`()
    {
        val repo = mock<RoleRepository> {
            on { getScopedReportReaderRoles("r1") } doReturn listOf(Role("Funders",
                    listOf(User("test.user", "Test User", "test@example.com"),
                            User("unknown", "unknown", "funder@example.com"),
                            User("funder.user", "unknown", "another@example.com")
                    ), listOf()))
        }

        val sut = RoleController(actionContextWithReport, repo, mock())
        val result = sut.getScopedReportReaders()
        assertThat(result.count()).isEqualTo(1)
        assertThat(result[0].name).isEqualTo("Funders")

        val members = result[0].members
        assertThat(members[0].displayName).isEqualTo("Test User")
        assertThat(members[1].displayName).isEqualTo("funder.user")
        assertThat(members[2].displayName).isEqualTo("funder@example.com")
        assertThat(members[0].email).isEqualTo("test@example.com")
        assertThat(members[1].email).isEqualTo("another@example.com")
        assertThat(members[2].email).isEqualTo("funder@example.com")
    }

    @Test
    fun `getScopedReportReaders orders user group view models alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getScopedReportReaderRoles("r1") } doReturn multipleRolesFromRepo
        }

        val sut = RoleController(actionContextWithReport, repo, mock())
        val result = sut.getScopedReportReaders()
        assertExpectedMultipleRoleViewModels(result)
    }

    @Test
    fun `getScopedReportReaders orders user group view model members alphabetically`()
    {
        val repo = mock<RoleRepository> {
            on { getScopedReportReaderRoles("r1") } doReturn roleForAlphabeticFromRepo
        }

        val sut = RoleController(actionContextWithReport,repo, mock())
        val result = sut.getScopedReportReaders()
        assertExpectedAlphabeticRoleMembers(result)
    }

    @Test
    fun `can get role names`()
    {
        val repo = mock<RoleRepository> {
            on { getAllRoleNames() } doReturn listOf("Science", "Funders")
        }
        val sut = RoleController(mock(), repo, mock())
        assertThat(sut.getAllRoleNames()).containsExactly("Science", "Funders")
    }

    @Test
    fun `throws exception when adding user if email is missing`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":role-id") } doReturn "GROUP1"
            on { this.postData<String>() } doReturn mapOf()
        }

        val sut = RoleController(actionContext, mock(), mock())
        Assertions.assertThatThrownBy { sut.addUser() }.isInstanceOf(MissingParameterError::class.java)
    }

    @Test
    fun `adds permission to user group`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":role-id") } doReturn "test"
            on { this.postData<String>() } doReturn mapOf(
                    "name" to "test.permission",
                    "scope_prefix" to "report",
                    "scope_id" to "report1"
            )
        }

        val authRepo = mock<AuthorizationRepository>()
        val sut = RoleController(actionContext, mock(), authRepo)
        val result = sut.addPermission()

        assertThat(result).isEqualTo("OK")

        val permissionCaptor: ArgumentCaptor<ReifiedPermission> = ArgumentCaptor.forClass(ReifiedPermission::class.java)
        verify(authRepo).ensureUserGroupHasPermission(eq("test"), capture(permissionCaptor))

        val permission = permissionCaptor.value
        assertThat(permission.name).isEqualTo("test.permission")
        assertThat(permission.scope.value).isEqualTo("report:report1")
    }

    @Test
    fun `removes permission from role`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":role-id") } doReturn "test"
            on { this.params(":name") } doReturn "test.permission"
        }

        val authRepo = mock<AuthorizationRepository>()
        val sut = RoleController(actionContext, mock(), authRepo)
        val result = sut.removePermission()

        assertThat(result).isEqualTo("OK")

        val permissionCaptor: ArgumentCaptor<ReifiedPermission> = ArgumentCaptor.forClass(ReifiedPermission::class.java)
        verify(authRepo).ensureUserGroupDoesNotHavePermission(eq("test"), capture(permissionCaptor))

        val permission = permissionCaptor.value
        assertThat(permission.name).isEqualTo("test.permission")
        assertThat(permission.scope.value).isEqualTo("*")
    }

    @Test
    fun `adds new role`()
    {
        val actionContext = mock<ActionContext> {
            on { this.postData<String>() } doReturn mapOf("name" to "NEWGROUP")
        }

        val authRepo = mock<AuthorizationRepository>()
        val sut = RoleController(actionContext, mock(), authRepo)
        sut.addRole()
        verify(authRepo, Times(1)).createUserGroup("NEWGROUP")
    }

    @Test
    fun `deletes role`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":role-id") } doReturn "TestRole"
        }

        val roleRepo = mock<RoleRepository> {
            on { this.getAllRoleNames() } doReturn listOf("TestRole")
        }

        val authRepo = mock<AuthorizationRepository>()

        val sut = RoleController(actionContext, roleRepo, authRepo)
        sut.deleteRole()
        verify(authRepo).deleteUserGroup("TestRole")
    }

    @Test
    fun `throws exception on delete Admin role`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":role-id") } doReturn "Admin"
        }
        val authRepo = mock<AuthorizationRepository>()

        val sut = RoleController(actionContext, mock(), authRepo)

        assertThatThrownBy { sut.deleteRole() }
                .isInstanceOf(InvalidOperationError::class.java)
                .hasMessageContaining("You cannot delete the Admin role")
        verify(authRepo, Times(0)).deleteUserGroup(any())
    }

    @Test
    fun `throws exception on delete non-existent role`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":role-id") } doReturn "TestRole"
        }

        val roleRepo = mock<RoleRepository> {
            on { this.getAllRoleNames() } doReturn listOf("DifferentRole")
        }

        val authRepo = mock<AuthorizationRepository>()

        val sut = RoleController(actionContext, roleRepo, authRepo)
        assertThatThrownBy { sut.deleteRole() }
                .isInstanceOf(UnknownObjectError::class.java)
                .hasMessageContaining("Unknown role")
        verify(authRepo, Times(0)).deleteUserGroup(any())
    }

    @Test
    fun `adds user to role`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":role-id") } doReturn "GROUP1"
            on { this.postData<String>() } doReturn mapOf("email" to "test@example.com")
        }

        val authRepo = mock<AuthorizationRepository>()
        val sut = RoleController(actionContext, mock(), authRepo)
        sut.addUser()
        verify(authRepo).ensureGroupHasMember("GROUP1", "test@example.com")
    }

    @Test
    fun `removes user from role`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":role-id") } doReturn "GROUP1"
            on { this.params(":email") } doReturn "test@example.com"
        }

        val authRepo = mock<AuthorizationRepository>()
        val sut = RoleController(actionContext, mock(), authRepo)
        sut.removeUser()
        verify(authRepo).ensureGroupDoesNotHaveMember("GROUP1", "test@example.com")
    }

    @Test
    fun `throws exception when adding role if name is missing`()
    {
        val actionContext = mock<ActionContext> {
            on { this.postData<String>() } doReturn mapOf()
        }

        val sut = RoleController(actionContext, mock(), mock())
        Assertions.assertThatThrownBy { sut.addRole() }.isInstanceOf(MissingParameterError::class.java)
    }

    @Test
    fun `throws exception if remove current user from Admin role`()
    {
        val mockProfile = mock<CommonProfile>{
            on { id } doReturn "test@example.com"
        }

        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn mockProfile
            on { params(":email") } doReturn "test@example.com"
            on { params(":role-id") } doReturn "Admin"
        }

        val sut = RoleController(mockContext, mock(), mock())
        Assertions.assertThatThrownBy { sut.removeUser() }
                .isInstanceOf(InvalidOperationError::class.java)
                .hasMessageContaining("You cannot remove yourself from the Admin role.")
    }

    @Test
    fun `does not throw exception if remove different user from Admin role`()
    {
        val mockProfile = mock<CommonProfile>{
            on { id } doReturn "test@example.com"
        }

        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn mockProfile
            on { params(":email") } doReturn "rogue-admin@example.com"
            on { params(":role-id") } doReturn "Admin"
        }

        val sut = RoleController(mockContext, mock(), mock())
        sut.removeUser();
    }
}