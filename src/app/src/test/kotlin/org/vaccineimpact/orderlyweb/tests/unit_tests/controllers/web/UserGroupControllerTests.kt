package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.UserGroupController
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.models.permissions.UserGroup
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.viewmodels.UserGroupViewModel

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
        val sut = UserGroupController(actionContext, authRepo, mock())
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
        val sut = UserGroupController(actionContext, authRepo, mock())
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

        val sut = UserGroupController(actionContext, mock(), mock())
        Assertions.assertThatThrownBy { sut.associatePermission() }.isInstanceOf(IllegalArgumentException::class.java)

    }

    @Test
    fun `getGlobalReportReaderGroups builds members user group view model`()
    {
        val repo = mock<UserRepository> {
            on { getGlobalReportReaderGroups() } doReturn listOf(UserGroup("Funders",
                    listOf(User("test.user", "Test User", "test@example.com"),
                            User("unknown", "unknown", "funder@example.com"),
                            User("funder.user", "unknown", "another@example.com")
                    )))
        }

        val sut = UserGroupController(mock(), mock(), repo)
        val result = sut.getGlobalReportReaders()
        assertThat(result.count()).isEqualTo(1)
        assertThat(result[0] is UserGroupViewModel.MembersGroupViewModel).isTrue()
        assertThat(result[0].name).isEqualTo("Funders")

        val members = result[0].members
        assertThat(members.all { !it.canRemove }).isTrue()
        assertThat(members[0].displayName).isEqualTo("Test User")
        assertThat(members[1].displayName).isEqualTo("funder.user")
        assertThat(members[2].displayName).isEqualTo("funder@example.com")
        assertThat(members[0].email).isEqualTo("test@example.com")
        assertThat(members[1].email).isEqualTo("another@example.com")
        assertThat(members[2].email).isEqualTo("funder@example.com")
    }

    @Test
    fun `getGlobalReportReaderGroups orders user group view models first by whether or not they have members then alphabetically`()
    {
        val repo = mock<UserRepository> {
            on { getGlobalReportReaderGroups() } doReturn listOf(
                    UserGroup("Science", listOf()),
                    UserGroup("test.user@example.com", listOf(
                            User("test.user", "Test User", "test.user@example.com"))
                    ),
                    UserGroup("Funders", listOf()),
                    UserGroup("Tech", listOf()),
                    UserGroup("another.user@example.com", listOf(
                            User("another.user", "A User", "another.user@example.com"))
                    )
            )
        }

        val sut = UserGroupController(mock(), mock(), repo)
        val result = sut.getGlobalReportReaders()
        assertThat(result[0].name).isEqualTo("Funders")
        assertThat(result[1].name).isEqualTo("Science")
        assertThat(result[2].name).isEqualTo("Tech")
        assertThat(result[3].name).isEqualTo("another.user@example.com")
        assertThat(result[4].name).isEqualTo("test.user@example.com")
    }

    @Test
    fun `getGlobalReportReaderGroups builds identity group view models`()
    {
        val repo = mock<UserRepository> {
            on { getGlobalReportReaderGroups() } doReturn listOf(
                    UserGroup("test.user@example.com", listOf(
                            User("test.user", "Test User", "test.user@example.com"))
                    )
            )
        }

        val sut = UserGroupController(mock(), mock(), repo)
        val result = sut.getGlobalReportReaders()
        assertThat(result[0] is UserGroupViewModel.IdentityGroupViewModel).isTrue()
        assertThat(result[0].members.count()).isEqualTo(0)
        assertThat(result[0].name).isEqualTo("test.user@example.com")
    }

}