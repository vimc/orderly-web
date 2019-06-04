package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.UserController
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import java.lang.IllegalArgumentException
import java.time.Instant

class UserControllerTests : TeamcityTests()
{
    private val mockAuthRepo = mock<OrderlyAuthorizationRepository>{}

    @Test
    fun `gets report readers`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":report") } doReturn "r1"
        }

        val reportReaders = mapOf(
                User("scoped.reader",
                        "unknown",
                        "scoped.reader@email.com",
                        "test",
                        Instant.now()) to Scope.Specific("report", "r1"),
                User("global.reader",
                        "Global Reader",
                        "global.reader@email.com",
                        "test",
                        Instant.now()) to Scope.Global()

        )

        val authRepo = mock<OrderlyAuthorizationRepository>{
            on { this.getReportReaders("r1")} doReturn(reportReaders)
        }
        val sut = UserController(actionContext, authRepo)
        val result = sut.getReportReaders()

        assertThat(result.count()).isEqualTo(2)

        assertThat(result[0].username).isEqualTo("global.reader") //Should have been sorted by username
        assertThat(result[0].displayName).isEqualTo("Global Reader")
        assertThat(result[0].canRemove).isFalse()

        assertThat(result[1].username).isEqualTo("scoped.reader")
        assertThat(result[1].displayName).isEqualTo("scoped.reader")
        assertThat(result[1].canRemove).isTrue()
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

        val authRepo = mock<OrderlyAuthorizationRepository>()
        val sut = UserController(actionContext, authRepo)
        val result = sut.associatePermission()

        assertThat(result).isEqualTo("OK")

        val permissionCaptor = ArgumentCaptor.forClass(ReifiedPermission::class.java)
        verify(authRepo).ensureUserGroupHasPermission("user1@example.com", permissionCaptor.capture())
        val permission = permissionCaptor.value

        assertThat(permission.name).isEqualTo("test.permission")
        assertThat(permission.scope.value).isEqualTo("report:report1")
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

        val authRepo = mock<OrderlyAuthorizationRepository>()
        val sut = UserController(actionContext, authRepo)
        assertThatThrownBy { sut.associatePermission() }.isInstanceOf(IllegalArgumentException::class.java)

    }

}