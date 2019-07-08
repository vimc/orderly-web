package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.UserController
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.User
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
                User("scoped.reader",
                        "Scoped Reader",
                        "scoped.reader@email.com") to
                        listOf(UserGroupPermission("scoped.reader@email.com", specificPerm)),
                User("global.reader",
                        "Global Reader",
                        "global.reader@email.com") to
                        listOf(UserGroupPermission("global.reader@email.com", globalPerm)),
                User("group.reader",
                        "Group Reader",
                        "group.reader@email.com") to
                        listOf(UserGroupPermission("group.reader@email.com", specificPerm),
                               UserGroupPermission("user.group", specificPerm))

        )

        val repo = mock<UserRepository>{
            on { this.getReportReaders("r1")} doReturn(reportReaders)
        }
        val sut = UserController(actionContext, repo)
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
                        "r1@email.com") to
                        listOf(UserGroupPermission("r1@email.com", globalPerm)),
                User("r2username",
                        "unknown",
                        "r2@email.com") to
                        listOf(UserGroupPermission("r2@email.com", globalPerm)),
                User("",
                        "",
                        "r3@email.com") to
                        listOf(UserGroupPermission("r3@email.com", globalPerm)),
                User("unknown",
                        "unknown",
                        "r4@email.com") to
                        listOf(UserGroupPermission("r4@email.com", globalPerm))

        )

        val repo = mock<UserRepository>{
            on { this.getReportReaders("r1")} doReturn(reportReaders)
        }
        val sut = UserController(actionContext, repo)
        val result = sut.getReportReaders()

        assertThat(result.count()).isEqualTo(4)

        assertThat(result[0].displayName).isEqualTo("r1username")
        assertThat(result[1].displayName).isEqualTo("r2username")
        assertThat(result[2].displayName).isEqualTo("r3@email.com")
        assertThat(result[3].displayName).isEqualTo("r4@email.com")
    }

}