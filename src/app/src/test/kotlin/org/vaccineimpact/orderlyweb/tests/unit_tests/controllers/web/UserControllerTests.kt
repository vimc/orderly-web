package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.UserController
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.User
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

        val reportReaders = mapOf(
                User("scoped.reader",
                        "Scoped Reader",
                        "scoped.reader@email.com",
                        "test",
                        Instant.now()) to Scope.Specific("report", "r1"),
                User("global.reader",
                        "Global Reader",
                        "global.reader@email.com",
                        "test",
                        Instant.now()) to Scope.Global()

        )

        val authRepo = mock<AuthorizationRepository>{
            on { this.getReportReaders("r1")} doReturn(reportReaders)
        }
        val sut = UserController(actionContext, authRepo)
        val result = sut.getReportReaders()

        assertThat(result.count()).isEqualTo(2)

        assertThat(result[0].username).isEqualTo("global.reader") //Should have been sorted by username
        assertThat(result[0].displayName).isEqualTo("Global Reader")
        assertThat(result[0].canRemove).isFalse()

        assertThat(result[1].username).isEqualTo("scoped.reader")
        assertThat(result[1].displayName).isEqualTo("Scoped Reader")
        assertThat(result[1].canRemove).isTrue()
    }

    @Test
    fun `gets report readers with fallback display names`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":report") } doReturn "r1"
        }

        val reportReaders = mapOf(
                User("r1username",
                        "",
                        "r1@email.com",
                        "test",
                        Instant.now()) to Scope.Global(),
                User("r2username",
                        "unknown",
                        "r2@email.com",
                        "test",
                        Instant.now()) to Scope.Global(),
                User("",
                        "",
                        "r3@email.com",
                        "test",
                        Instant.now()) to Scope.Global(),
                User("unknown",
                        "unknown",
                        "r4@email.com",
                        "test",
                        Instant.now()) to Scope.Global()

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