package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.UserController
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class UserControllerTests : TeamcityTests()
{
    @Test
    fun `gets user emails`()
    {
        val repo = mock<UserRepository> {
            on { this.getUserEmails() } doReturn (listOf("one", "two"))
        }

        val sut = UserController(mock(), repo, mock())

        assertThat(sut.getUserEmails()).containsExactlyElementsOf(listOf("one", "two"))
    }

    @Test
    fun `gets all users and permissions`()
    {
        val repo = mock<UserRepository> {
            on { this.getAllUsersWithPermissions() } doReturn
                    listOf(User("test.user", "Test user", "test@test.com"),
                            User("another.user", "A user", "a@test.com"))
        }

        val authRepo = mock<AuthorizationRepository> {
            on { this.getPermissionsForUser("test@test.com") } doReturn PermissionSet("*/reports.review", "*/reports.read")
            on { this.getPermissionsForUser("a@test.com") } doReturn PermissionSet()
        }

        val sut = UserController(mock(), repo, authRepo)

        val result = sut.getAllUsers()

        val firstUser = result[0]
        val secondUser = result[1]

        assertThat(secondUser.displayName).isEqualTo("Test user")
        assertThat(secondUser.email).isEqualTo("test@test.com")
        assertThat(secondUser.username).isEqualTo("test.user")
        assertThat(secondUser.permissions[0].name).isEqualTo("reports.read")
        assertThat(secondUser.permissions[0].scope).isEqualTo("*")
        assertThat(secondUser.permissions[1].name).isEqualTo("reports.review")
        assertThat(secondUser.permissions[1].scope).isEqualTo("*")

        assertThat(firstUser.displayName).isEqualTo("A user")
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
        val sut = UserController(actionContext, repo, mock())
        val result = sut.getScopedReportReaders()

        assertThat(result.count()).isEqualTo(3)

        assertThat(result[0].username).isEqualTo("global.reader") //Should have been sorted by display name
        assertThat(result[0].displayName).isEqualTo("Global Reader")

        assertThat(result[1].username).isEqualTo("group.reader")
        assertThat(result[1].displayName).isEqualTo("Group Reader")

        assertThat(result[2].username).isEqualTo("scoped.reader")
        assertThat(result[2].displayName).isEqualTo("Scoped Reader")

    }

    @Test
    fun `gets global report readers`()
    {
        val actionContext = mock<ActionContext>()
        val reportReaders = listOf(
                User("global.reader",
                        "Global Reader",
                        "global.reader@email.com"),
                User("global.reader.2",
                        "A Global Reader2",
                        "global.reader2@email.com")
        )

        val repo = mock<UserRepository> {
            on { this.getGlobalReportReaderUsers() } doReturn (reportReaders)
        }

        val sut = UserController(actionContext, repo, mock())
        val result = sut.getGlobalReportReaders()

        assertThat(result.count()).isEqualTo(2)

        assertThat(result[0].username).isEqualTo("global.reader.2") //Should have been sorted by displayname
        assertThat(result[0].displayName).isEqualTo("A Global Reader2")
        assertThat(result[0].email).isEqualTo("global.reader2@email.com")

        assertThat(result[1].username).isEqualTo("global.reader")
        assertThat(result[1].displayName).isEqualTo("Global Reader")
        assertThat(result[1].email).isEqualTo("global.reader@email.com")
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
        val sut = UserController(actionContext, repo, mock())
        val result = sut.getScopedReportReaders()

        assertThat(result.count()).isEqualTo(4)

        assertThat(result[0].displayName).isEqualTo("r1username")
        assertThat(result[1].displayName).isEqualTo("r2username")
        assertThat(result[2].displayName).isEqualTo("r3@email.com")
        assertThat(result[3].displayName).isEqualTo("r4@email.com")
    }

}