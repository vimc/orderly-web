package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.UserController
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.UserDetails
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.models.permissions.UserGroupPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import java.time.Instant

class UserControllerTests : TeamcityTests()
{

    @Test
    fun `gets report readers`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":report") } doReturn "r1"
        }

        val specificPerm = ReifiedPermission("reports.read", Scope.Specific("report", "r1"))
        val globalPerm = ReifiedPermission("reports.read", Scope.Global())

        val reportReaders = mapOf(
                UserDetails("scoped.reader",
                        "Scoped Reader",
                        "scoped.reader@email.com",
                        "test",
                        Instant.now()) to
                        listOf(UserGroupPermission("scoped.reader@email.com", specificPerm)),
                UserDetails("global.reader",
                        "Global Reader",
                        "global.reader@email.com",
                        "test",
                        Instant.now()) to
                        listOf(UserGroupPermission("global.reader@email.com", globalPerm)),
                UserDetails("group.reader",
                        "Group Reader",
                        "group.reader@email.com",
                        "test",
                        Instant.now()) to
                        listOf(UserGroupPermission("group.reader@email.com", specificPerm),
                               UserGroupPermission("user.group", specificPerm))

        )

        val authRepo = mock<AuthorizationRepository>{
            on { this.getReportReaders("r1")} doReturn(reportReaders)
        }
        val sut = UserController(actionContext, authRepo)
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
                UserDetails("r1username",
                        "",
                        "r1@email.com",
                        "test",
                        Instant.now()) to
                        listOf(UserGroupPermission("r1@email.com", globalPerm)),
                UserDetails("r2username",
                        "unknown",
                        "r2@email.com",
                        "test",
                        Instant.now()) to
                        listOf(UserGroupPermission("r2@email.com", globalPerm)),
                UserDetails("",
                        "",
                        "r3@email.com",
                        "test",
                        Instant.now()) to
                        listOf(UserGroupPermission("r3@email.com", globalPerm)),
                UserDetails("unknown",
                        "unknown",
                        "r4@email.com",
                        "test",
                        Instant.now()) to
                        listOf(UserGroupPermission("r4@email.com", globalPerm))

        )

        val authRepo = mock<AuthorizationRepository>{
            on { this.getReportReaders("r1")} doReturn(reportReaders)
        }
        val sut = UserController(actionContext, authRepo)
        val result = sut.getReportReaders()

        assertThat(result.count()).isEqualTo(4)

        assertThat(result[0].displayName).isEqualTo("r1username")
        assertThat(result[1].displayName).isEqualTo("r2username")
        assertThat(result[2].displayName).isEqualTo("r3@email.com")
        assertThat(result[3].displayName).isEqualTo("r4@email.com")
    }

}