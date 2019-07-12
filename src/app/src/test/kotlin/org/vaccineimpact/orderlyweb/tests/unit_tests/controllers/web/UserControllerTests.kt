package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.UserController
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class UserControllerTests : TeamcityTests()
{
    @Test
    fun `gets user emails`()
    {
        val repo = mock<UserRepository> {
            on { this.getUserEmails() } doReturn (listOf("one", "two"))
        }

        val sut = UserController(mock(), repo)

        assertThat(sut.getUserEmails()).containsExactlyElementsOf(listOf("one", "two"))
    }

    @Test
    fun `gets individual scoped report readers`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":report") } doReturn "r1"
        }

        val reportReaders = listOf(
                User("scoped.reader",
                        "Scoped Reader",
                        "scoped.reader@email.com"),
                User("global.reader",
                        "Global Reader",
                        "global.reader@email.com"),
                User("group.reader",
                        "Group Reader",
                        "group.reader@email.com")
        )

        val repo = mock<UserRepository> {
            on { this.getScopedReportReaderUsers("r1") } doReturn (reportReaders)
        }
        val sut = UserController(actionContext, repo)
        val result = sut.getScopedReportReaders()

        assertThat(result.count()).isEqualTo(3)

        assertThat(result[0].username).isEqualTo("global.reader") //Should have been sorted by username
        assertThat(result[0].displayName).isEqualTo("Global Reader")

        assertThat(result[1].username).isEqualTo("group.reader")
        assertThat(result[1].displayName).isEqualTo("Group Reader")

        assertThat(result[2].username).isEqualTo("scoped.reader")
        assertThat(result[2].displayName).isEqualTo("Scoped Reader")

    }

    @Test
    fun `gets report readers with fallback display names`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":report") } doReturn "r1"
        }

        val reportReaders = listOf(
                User("r1username",
                        "",
                        "r1@email.com"),
                User("r2username",
                        "unknown",
                        "r2@email.com"),
                User("",
                        "",
                        "r3@email.com"),
                User("unknown",
                        "unknown",
                        "r4@email.com"))

        val repo = mock<UserRepository> {
            on { this.getScopedReportReaderUsers("r1") } doReturn (reportReaders)
        }
        val sut = UserController(actionContext, repo)
        val result = sut.getScopedReportReaders()

        assertThat(result.count()).isEqualTo(4)

        assertThat(result[0].displayName).isEqualTo("r1username")
        assertThat(result[1].displayName).isEqualTo("r2username")
        assertThat(result[2].displayName).isEqualTo("r3@email.com")
        assertThat(result[3].displayName).isEqualTo("r4@email.com")
    }

}