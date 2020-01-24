package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.internal.verification.Times
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.UserGroupController
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.viewmodels.Breadcrumb
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel

class UserGroupControllerTests : TeamcityTests()
{
    @Test
    fun `adds permission to user group`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":user-group-id") } doReturn "user1@example.com"
            on { this.postData() } doReturn mapOf(
                    "action" to "add",
                    "name" to "test.permission",
                    "scope_prefix" to "report",
                    "scope_id" to "report1"
            )
        }

        val authRepo = mock<AuthorizationRepository>()
        val sut = UserGroupController(actionContext, authRepo)
        val result = sut.associatePermission()

        assertThat(result).isEqualTo("OK")

        val permissionCaptor: ArgumentCaptor<ReifiedPermission> = ArgumentCaptor.forClass(ReifiedPermission::class.java)
        verify(authRepo).ensureUserGroupHasPermission(eq("user1@example.com"), capture(permissionCaptor))

        val permission = permissionCaptor.value
        Assertions.assertThat(permission.name).isEqualTo("test.permission")
        Assertions.assertThat(permission.scope.value).isEqualTo("report:report1")
    }

    @Test
    fun `removes permission from user group`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":user-group-id") } doReturn "user1@example.com"
            on { this.postData() } doReturn mapOf(
                    "action" to "remove",
                    "name" to "test.permission",
                    "scope_prefix" to "report",
                    "scope_id" to "report1"
            )
        }

        val authRepo = mock<AuthorizationRepository>()
        val sut = UserGroupController(actionContext, authRepo)
        val result = sut.associatePermission()


        assertThat(result).isEqualTo("OK")

        val permissionCaptor: ArgumentCaptor<ReifiedPermission> = ArgumentCaptor.forClass(ReifiedPermission::class.java)
        verify(authRepo).ensureUserGroupDoesNotHavePermission(eq("user1@example.com"), capture(permissionCaptor))

        val permission = permissionCaptor.value
        Assertions.assertThat(permission.name).isEqualTo("test.permission")
        Assertions.assertThat(permission.scope.value).isEqualTo("report:report1")
    }

    @Test
    fun `throws exception if associate permission with unknown action`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":user-group-id") } doReturn "user1@example.com"
            on { this.postData() } doReturn mapOf(
                    "action" to "addx",
                    "name" to "test.permission",
                    "scope_prefix" to "report",
                    "scope_id" to "report1"
            )
        }

        val sut = UserGroupController(actionContext, mock())
        Assertions.assertThatThrownBy { sut.associatePermission() }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `adds new user group`()
    {
        val actionContext = mock<ActionContext> {
            on { this.postData() } doReturn mapOf("name" to "NEWGROUP")
        }

        val authRepo = mock<AuthorizationRepository>()
        val sut = UserGroupController(actionContext, authRepo)
        sut.addUserGroup()
        verify(authRepo, Times(1)).createUserGroup("NEWGROUP")
    }

    @Test
    fun `adds user to user group`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":user-group-id") } doReturn "GROUP1"
            on { this.postData() } doReturn mapOf("email" to "test@example.com")
        }

        val authRepo = mock<AuthorizationRepository>()
        val sut = UserGroupController(actionContext, authRepo)
        sut.addUser()
        verify(authRepo).ensureGroupHasMember("GROUP1", "test@example.com")
    }

    @Test
    fun `removes user from user group`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":user-group-id") } doReturn "GROUP1"
            on { this.params(":email") } doReturn "test@example.com"
        }

        val authRepo = mock<AuthorizationRepository>()
        val sut = UserGroupController(actionContext, authRepo)
        sut.removeUser()
        verify(authRepo).ensureGroupDoesNotHaveMember("GROUP1", "test@example.com")
    }

    @Test
    fun `throws exception when adding user group if name is missing`()
    {
        val actionContext = mock<ActionContext> {
            on { this.postData() } doReturn mapOf()
        }

        val sut = UserGroupController(actionContext, mock())
        Assertions.assertThatThrownBy { sut.addUserGroup() }.isInstanceOf(MissingParameterError::class.java)
    }

    @Test
    fun `returns correct breadcrumbs for admin page`()
    {
        val sut = UserGroupController(mock(), mock())
        val model = sut.admin()
        assertThat(model.breadcrumbs).containsExactly(IndexViewModel.breadcrumb,
                Breadcrumb("Admin", "http://localhost:8888/admin"))
    }

    @Test
    fun `throws exception when adding user if email is missing`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":user-group-id") } doReturn "GROUP1"
            on { this.postData() } doReturn mapOf()
        }

        val sut = UserGroupController(actionContext, mock())
        Assertions.assertThatThrownBy { sut.addUser() }.isInstanceOf(MissingParameterError::class.java)
    }

}