package org.vaccineimpact.orderlyweb.tests.security

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions
import org.eclipse.egit.github.core.Team
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.GitHubResponse
import org.eclipse.egit.github.core.client.RequestException
import org.junit.Assert
import org.junit.Test
import org.pac4j.core.exception.CredentialsException
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.BadConfigurationError
import org.vaccineimpact.orderlyweb.security.providers.GithubApiClientAuthHelper
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class GithubApiClientAuthHelperTests : TeamcityTests()
{
    private val mockTeam = Team().apply {
        name = "teamName"
        id = 1
    }

    private val mockUser = User().apply {
        login = "user.name"
        email = null
        name = "full name"
    }

    private val mockOrg = User().apply {
        login = "orgName"
    }

    private val mockAppConfig = mock<Config> {
        on { get("auth.github_org") } doReturn "orgName"
        on { get("auth.github_team") } doReturn "teamName"
    }

    private val mockGithubApiClient = mock<GitHubClient> {
        on { get(argWhere { it.uri.contains("/user/emails") }) } doReturn
                GitHubResponse(mock(), listOf("privateEmail"))
        on { get(argWhere { it.uri.endsWith("/user") }) } doReturn
                GitHubResponse(mock(), mockUser)
        on { get(argWhere { it.uri.contains("user/orgs") }) } doReturn
                GitHubResponse(mock(), listOf(mockOrg))
        on { get(argWhere { it.uri.contains("orgs/orgName/teams") }) } doReturn
                GitHubResponse(mock(), listOf(mockTeam))
        on { get(argWhere { it.uri.contains("teams/1/members") }) } doReturn
                GitHubResponse(mock(), listOf(mockUser))

    }

    @Test
    fun `initialise fails if token is blank`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)

        Assertions.assertThatThrownBy { sut.authenticate("") }
                .isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("Token cannot be blank")
    }

    @Test
    fun `can initialise`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        sut.authenticate("token")
        verify(mockGithubApiClient).setOAuth2Token("token")
    }

    @Test
    fun `checkGithubUserCanAuthenticate succeeds when user is member of allowed org and team`()
    {
        //default mock values should succeed
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        sut.authenticate("token")
        sut.checkGithubUserHasOrderlyWebAccess()
    }

    @Test
    fun `can getUser`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        sut.authenticate("token")
        val result = sut.getUser()
        Assert.assertSame(mockUser, result)
    }

    @Test
    fun `can getUserEmail when email is public`()
    {
        mockUser.email = "publicEmail"
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        sut.authenticate("token")
        val result = sut.getUserEmail()
        Assert.assertEquals("publicEmail", result)
    }

    @Test
    fun `can getUserEmail when email is private`()
    {
        //defaults should get the sut to find null email in user so fetch details from client
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        sut.authenticate("token")
        val result = sut.getUserEmail()
        Assert.assertEquals("privateEmail", result)
    }

    @Test
    fun `on checkGithubUserHasOrderlyWebAccess, IllegalStateException thrown if not authenticated`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        Assertions.assertThatThrownBy {
            sut.checkGithubUserHasOrderlyWebAccess()
        }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("User has not been authenticated")
    }

    @Test
    fun `on getUser, IllegalStateException thrown if not authenticated`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        Assertions.assertThatThrownBy {
            sut.getUser()
        }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("User has not been authenticated")
    }

    @Test
    fun `on getUserEmail, IllegalStateException thrown if not authenticated`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        Assertions.assertThatThrownBy {
            sut.getUserEmail()
        }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("User has not been authenticated")
    }

    @Test
    fun `on check access, BadConfigurationError is thrown if team does not exist`()
    {
        val customMockGithubApiClient = mock<GitHubClient> {
            on { get(argWhere { it.uri.contains("user") }) } doReturn
                    GitHubResponse(mock(), mockUser)
            on { get(argWhere { it.uri.contains("user/orgs") }) } doReturn
                    GitHubResponse(mock(), listOf(mockOrg))
            on { get(argWhere { it.uri.endsWith("orgs/orgName/teams") }) } doReturn GitHubResponse(mock(),
                    listOf<Team>())
        }

        val mockAppConfig = mock<Config> {
            on { get("auth.github_org") } doReturn "orgName"
            on { get("auth.github_team") } doReturn "teamName"
        }

        val sut = GithubApiClientAuthHelper(mockAppConfig, customMockGithubApiClient)

        sut.authenticate("token")

        Assertions.assertThatThrownBy {
            sut.checkGithubUserHasOrderlyWebAccess()
        }.isInstanceOf(BadConfigurationError::class.java)
                .hasMessageContaining("GitHub org orgName has no team called teamName")
    }

    @Test
    fun `on check access, CredentialsException is thrown if user does not belong to GitHub org`()
    {
        val customMockGithubApiClient = mock<GitHubClient> {
            on { get(argWhere { it.uri.endsWith("user") }) } doReturn
                    GitHubResponse(mock(), mockUser)
            on { get(argWhere { it.uri.contains("user/orgs") } ) } doReturn GitHubResponse(mock(),
                    listOf<User>())
        }

        val mockAppConfig = mock<Config> {
            on { get("auth.github_org") } doReturn "orgName"
        }

        val sut = GithubApiClientAuthHelper(mockAppConfig, customMockGithubApiClient)

        sut.authenticate("token")

        Assertions.assertThatThrownBy {
            sut.checkGithubUserHasOrderlyWebAccess()
        }.isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("User is not a member of GitHub org orgName")
    }

    @Test
    fun `on check access, CredentialsException is thrown if user does not belong to GitHub team`()
    {
        val customMockGithubApiClient = mock<GitHubClient> {
            on { get(argWhere { it.uri.endsWith("user") }) } doReturn
                    GitHubResponse(mock(), mockUser)
            on { get(argWhere { it.uri.contains("user/orgs") }) } doReturn
                    GitHubResponse(mock(), listOf(mockOrg))
            on { get(argWhere { it.uri.contains("orgs/orgName/teams") }) } doReturn
                    GitHubResponse(mock(), listOf(mockTeam))
            on { get(argWhere { it.uri.contains("teams/1/members") }) } doReturn
                    GitHubResponse(mock(), listOf<User>())
        }

        val sut = GithubApiClientAuthHelper(mockAppConfig, customMockGithubApiClient)

        sut.authenticate("token")

        Assertions.assertThatThrownBy {
            sut.checkGithubUserHasOrderlyWebAccess()
        }.isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("User is not a member of GitHub team teamName")
    }

    @Test
    fun `on getUserEmail, CredentialsException is thrown if email is not public and token does not have email scope`()
    {
        val userWithNullEmail = mockUser.apply { email = null }
        val customMockGithubApiClient = mock<GitHubClient> {
            on { get(argWhere { it.uri.endsWith("user") }) } doReturn
                    GitHubResponse(mock(), userWithNullEmail)
            on { get(argWhere { it.uri.contains("user/orgs") }) } doReturn
                    GitHubResponse(mock(), listOf(mockOrg))
            on { get(argWhere { it.uri.contains("orgs/orgName/teams") }) } doReturn
                    GitHubResponse(mock(), listOf(mockTeam))
            on { get(argWhere { it.uri.contains("teams/1/members") }) } doReturn
                    GitHubResponse(mock(), listOf(userWithNullEmail))
            on { get(argWhere { it.uri.contains("emails") }) } doThrow RequestException(mock(), 404)
        }

        val sut = GithubApiClientAuthHelper(mockAppConfig, customMockGithubApiClient)

        sut.authenticate("token")

        Assertions.assertThatThrownBy {
            sut.getUserEmail()
        }.isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("GitHub token must include scope user:email")
    }

    @Test
    fun `on check access, Exception is thrown if org membership is required and token does not have read user scope`()
    {
        val customMockGithubApiClient = mock<GitHubClient> {
            on { get(argWhere { it.uri.contains("user") }) } doReturn
                    GitHubResponse(mock(), mockUser)
            on { get(argWhere { it.uri.contains("user/orgs") }) } doThrow RequestException(mock(), 403)
        }

        val sut = GithubApiClientAuthHelper(mockAppConfig, customMockGithubApiClient)

        sut.authenticate("token")

        Assertions.assertThatThrownBy {
            sut.checkGithubUserHasOrderlyWebAccess()
        }.isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("GitHub token must include scope user:read")

    }

}