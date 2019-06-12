package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.UserController
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.models.permissions.UserGroupPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import java.lang.IllegalArgumentException
import java.time.Instant

class UserControllerTests : TeamcityTests()
{
    private val mockUserRepo = mock<UserRepository> {
    on { this.getUser("user1@example.com") } doReturn User("user.1",
            "User One",
            "user1@example.com",
            "test",
            Instant.now())
}

    @Test
    fun `gets report readers`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":report") } doReturn "r1"
        }

        val specificPerm = ReifiedPermission("reports.read", Scope.Specific("report", "r1"))
        val globalPerm = ReifiedPermission("reports.read", Scope.Global())

        val reportReaders = mapOf(
                User("scoped.reader",
                        "Scoped Reader",
                        "scoped.reader@email.com",
                        "test",
                        Instant.now()) to
                        listOf(UserGroupPermission("scoped.reader@email.com", specificPerm)),
                User("global.reader",
                        "Global Reader",
                        "global.reader@email.com",
                        "test",
                        Instant.now()) to
                        listOf(UserGroupPermission("global.reader@email.com", globalPerm)),
                User("group.reader",
                        "Group Reader",
                        "group.reader@email.com",
                        "test",
                        Instant.now()) to
                        listOf(UserGroupPermission("group.reader@email.com", specificPerm),
                               UserGroupPermission("user.group", specificPerm))

        )

        val userRepo = mock<UserRepository>()

        val authRepo = mock<AuthorizationRepository>{
            on { this.getReportReaders("r1")} doReturn(reportReaders)
        }
        val sut = UserController(actionContext, authRepo, userRepo)
        val result = sut.getReportReaders()

        assertThat(result.count()).isEqualTo(3)

        assertThat(result[0].username).isEqualTo("global.reader") //Should have been sorted by username
        assertThat(result[0].displayName).isEqualTo("Global Reader")
        assertThat(result[0].canRemove).isFalse()

        assertThat(result[1].username).isEqualTo("group.reader")
        assertThat(result[1].displayName).isEqualTo("Group Reader")
        assertThat(result[1].canRemove).isFalse()

        assertThat(result[2].username).isEqualTo("scoped.reader")
        assertThat(result[2].displayName).isEqualTo("Scoped Reader")
        assertThat(result[2].canRemove).isTrue()
    }

    @Test
    fun `gets report readers with fallback display names`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":report") } doReturn "r1"
        }

        val globalPerm = ReifiedPermission("reports.read", Scope.Global())

        val reportReaders = mapOf(
                User("r1username",
                        "",
                        "r1@email.com",
                        "test",
                        Instant.now()) to
                        listOf(UserGroupPermission("r1@email.com", globalPerm)),
                User("r2username",
                        "unknown",
                        "r2@email.com",
                        "test",
                        Instant.now()) to
                        listOf(UserGroupPermission("r2@email.com", globalPerm)),
                User("",
                        "",
                        "r3@email.com",
                        "test",
                        Instant.now()) to
                        listOf(UserGroupPermission("r3@email.com", globalPerm)),
                User("unknown",
                        "unknown",
                        "r4@email.com",
                        "test",
                        Instant.now()) to
                        listOf(UserGroupPermission("r4@email.com", globalPerm))

        )

        val userRepo = mock<UserRepository>()

        val authRepo = mock<AuthorizationRepository>{
            on { this.getReportReaders("r1")} doReturn(reportReaders)
        }
        val sut = UserController(actionContext, authRepo, userRepo)
        val result = sut.getReportReaders()

        assertThat(result.count()).isEqualTo(4)

        assertThat(result[0].displayName).isEqualTo("r1username")
        assertThat(result[1].displayName).isEqualTo("r2username")
        assertThat(result[2].displayName).isEqualTo("r3@email.com")
        assertThat(result[3].displayName).isEqualTo("r4@email.com")
    }

    @Test
    fun `adds permission to user`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":email") } doReturn "user1%40example.com"
            on { this.postData() } doReturn mapOf(
                    "action" to "add",
                    "name" to "test.permission",
                    "scope_prefix" to "report",
                    "scope_id" to "report1"
            )
        }

        val authRepo = mock<AuthorizationRepository>()
        val sut = UserController(actionContext, authRepo, mockUserRepo)
        val result = sut.associatePermission()

        assertThat(result).isEqualTo("OK")

        val permissionCaptor: ArgumentCaptor<ReifiedPermission>  = ArgumentCaptor.forClass(ReifiedPermission::class.java)
        verify(authRepo).ensureUserGroupHasPermission(eq("user1@example.com"), capture(permissionCaptor))

        val permission = permissionCaptor.value
        assertThat(permission.name).isEqualTo("test.permission")
        assertThat(permission.scope.value).isEqualTo("report:report1")
    }

    @Test
    fun `removes permission from user`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":email") } doReturn "user1%40example.com"
            on { this.postData() } doReturn mapOf(
                    "action" to "remove",
                    "name" to "test.permission",
                    "scope_prefix" to "report",
                    "scope_id" to "report1"
            )
        }

        val authRepo = mock<AuthorizationRepository>()
        val sut = UserController(actionContext, authRepo, mockUserRepo)
        val result = sut.associatePermission()

        assertThat(result).isEqualTo("OK")

        val permissionCaptor: ArgumentCaptor<ReifiedPermission>  = ArgumentCaptor.forClass(ReifiedPermission::class.java)
        verify(authRepo).ensureUserGroupDoesNotHavePermission(eq("user1@example.com"), capture(permissionCaptor))

        val permission = permissionCaptor.value
        assertThat(permission.name).isEqualTo("test.permission")
        assertThat(permission.scope.value).isEqualTo("report:report1")
    }

    @Test
    fun `throws exception if associate permission for non-user`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":email") } doReturn "user1%40example.com"
            on { this.postData() } doReturn mapOf(
                    "action" to "add",
                    "name" to "test.permission",
                    "scope_prefix" to "report",
                    "scope_id" to "report1"
            )
        }

        val userRepo = mock<UserRepository> {
        }

        val authRepo = mock<AuthorizationRepository>()
        val sut = UserController(actionContext, authRepo, userRepo)
        assertThatThrownBy{ sut.associatePermission() }.isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `throws exception if associate permission with unknown action`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":email") } doReturn "user1%40example.com"
            on { this.postData() } doReturn mapOf(
                    "action" to "addx",
                    "name" to "test.permission",
                    "scope_prefix" to "report",
                    "scope_id" to "report1"
            )
        }

        val userRepo = mock<UserRepository> {
            on { this.getUser("user1@example.com") } doReturn User("user.1",
                    "User One",
                    "user1@example.com",
                    "test",
                    Instant.now())
        }

        val authRepo = mock<AuthorizationRepository>()
        val sut = UserController(actionContext, authRepo, userRepo)
        assertThatThrownBy { sut.associatePermission() }.isInstanceOf(IllegalArgumentException::class.java)

    }

}