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
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.BadConfigurationError
import org.vaccineimpact.orderlyweb.security.authentication.GithubAuthenticator
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

    private val mockAppConfig = mock<Config> {
        on { get("auth.github_org") } doReturn "orgName"
        on { get("auth.github_team") } doReturn "teamName"
    }

    private val mockGithubApiClient = mock<GitHubClient> {
        on { get(argWhere { it.uri.contains("/user/emails") }) } doReturn
                GitHubResponse(mock(), listOf("privateEmail"))
        on { get(argWhere { it.uri.endsWith("/user") }) } doReturn
                GitHubResponse(mock(), mockUser)
        on { get(argWhere { it.uri.contains("orgs/orgName/members") }) } doReturn
                GitHubResponse(mock(), listOf(mockUser))
        on { get(argWhere { it.uri.contains("orgs/orgName/teams") }) } doReturn
                GitHubResponse(mock(), listOf(mockTeam))
        on { get(argWhere { it.uri.contains("teams/1/members") }) } doReturn
                GitHubResponse(mock(), listOf(mockUser))

    }

    @Test
    fun `initialise fails if token is blank`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)

        Assertions.assertThatThrownBy { sut.initialise("") }
                .isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("Token cannot be blank")
    }

    @Test
    fun `can initialise`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        sut.initialise("token")
        verify(mockGithubApiClient).setOAuth2Token("token")
    }

    @Test
    fun `checkGithubUserCanAuthenticate succeeds when user is member of allowed org and team`()
    {
        //default mock values should succeed
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        sut.initialise("token")
        sut.checkGithubUserCanAuthenticate()
    }

    @Test
    fun `can getUser`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        sut.initialise("token")
        val result = sut.getUser()
        Assert.assertSame(mockUser, result)
    }

    @Test
    fun `can getUserEmail when email is public`()
    {
        mockUser.email = "publicEmail"
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        sut.initialise("token")
        val result = sut.getUserEmail()
        Assert.assertEquals("publicEmail", result)
    }

    @Test
    fun `can getUserEmail when email is private`()
    {
        //defaults should get the sut to find null email in user so fetch details from client
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        sut.initialise("token")
        val result = sut.getUserEmail()
        Assert.assertEquals("privateEmail", result)
    }

    @Test
    fun `on checkGithubUserCanAuthenticate, IllegalStateException thrown if not initialized`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        Assertions.assertThatThrownBy {
            sut.checkGithubUserCanAuthenticate()
        }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("User has not been initialized")
    }

    @Test
    fun `on getUser, IllegalStateException thrown if not initialized`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        Assertions.assertThatThrownBy {
            sut.getUser()
        }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("User has not been initialized")
    }

    @Test
    fun `on getUserEmail, IllegalStateException thrown if not initialized`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubApiClient)
        Assertions.assertThatThrownBy {
            sut.getUserEmail()
        }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("User has not been initialized")
    }

    @Test
    fun `on checkGithubUserCanAuthenticate, BadConfigurationError is thrown if GitHub org does not exist`()
    {
        val customMockGithubApiClient = mock<GitHubClient> {
            on { get(argWhere { it.uri.contains("user") }) } doReturn
                    GitHubResponse(mock(), mockUser)
            on { get(argWhere { it.uri.contains("nonsense") }) } doThrow RequestException(mock(), 404)
        }

        val mockAppConfig = mock<Config> {
            on { get("auth.github_org") } doReturn "nonsense"
            on { get("auth.github_team") } doReturn ""
        }

        val sut = GithubApiClientAuthHelper(mockAppConfig, customMockGithubApiClient)

        sut.initialise("token")
        Assertions.assertThatThrownBy {
            sut.checkGithubUserCanAuthenticate()
        }.isInstanceOf(BadConfigurationError::class.java)
                .hasMessageContaining("GitHub org nonsense does not exist")

    }

    @Test
    fun `on checkGithubUserCanAuthenticate, BadConfigurationError is thrown if team does not exist`()
    {
        val customMockGithubApiClient = mock<GitHubClient> {
            on { get(argWhere { it.uri.contains("user") }) } doReturn
                    GitHubResponse(mock(), mockUser)
            on { get(argWhere { it.uri.endsWith("orgs/orgName/members") }) } doReturn GitHubResponse(mock(),
                    listOf(mockUser))
            on { get(argWhere { it.uri.endsWith("orgs/orgName/teams") }) } doReturn GitHubResponse(mock(),
                    listOf<Team>())
        }

        val mockAppConfig = mock<Config> {
            on { get("auth.github_org") } doReturn "orgName"
            on { get("auth.github_team") } doReturn "teamName"
        }

        val sut = GithubApiClientAuthHelper(mockAppConfig, customMockGithubApiClient)

        sut.initialise("token")

        Assertions.assertThatThrownBy {
            sut.checkGithubUserCanAuthenticate()
        }.isInstanceOf(BadConfigurationError::class.java)
                .hasMessageContaining("GitHub org orgName has no team called teamName")
    }

    @Test
    fun `on checkGithubUserCanAuthenticate, CredentialsException is thrown if user does not belong to GitHub org`()
    {
        val customMockGithubApiClient = mock<GitHubClient> {
            on { get(argWhere { it.uri.contains("user") }) } doReturn
                    GitHubResponse(mock(), mockUser)
            on { get(argWhere { it.uri.contains("orgName") }) } doReturn GitHubResponse(mock(),
                    listOf<User>())
        }

        val mockAppConfig = mock<Config> {
            on { get("auth.github_org") } doReturn "orgName"
        }

        val sut = GithubApiClientAuthHelper(mockAppConfig, customMockGithubApiClient)

        sut.initialise("token")

        Assertions.assertThatThrownBy {
            sut.checkGithubUserCanAuthenticate()
        }.isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("User is not a member of GitHub org orgName or token does not include read:org scope")
    }

    @Test
    fun `on checkGithubUserCanAuthenticate, CredentialsException is thrown if user does not belong to GitHub team`()
    {
        val customMockGithubApiClient = mock<GitHubClient> {
            on { get(argWhere { it.uri.contains("user") }) } doReturn
                    GitHubResponse(mock(), mockUser)
            on { get(argWhere { it.uri.contains("orgs/orgName/members") }) } doReturn
                    GitHubResponse(mock(), listOf(mockUser))
            on { get(argWhere { it.uri.contains("orgs/orgName/teams") }) } doReturn
                    GitHubResponse(mock(), listOf(mockTeam))
            on { get(argWhere { it.uri.contains("teams/1/members") }) } doReturn
                    GitHubResponse(mock(), listOf<User>())
        }

        val sut = GithubApiClientAuthHelper(mockAppConfig, customMockGithubApiClient)

        sut.initialise("token")

        Assertions.assertThatThrownBy {
            sut.checkGithubUserCanAuthenticate()
        }.isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("User is not a member of GitHub team teamName")
    }

    @Test
    fun `on getUserEmail, CredentialsException is thrown if email is not public and token does not have email scope`()
    {
        val userWithNullEmail = mockUser.apply { email = null }
        val customMockGithubApiClient = mock<GitHubClient> {
            on { get(argWhere { it.uri.contains("user") }) } doReturn
                    GitHubResponse(mock(), userWithNullEmail)
            on { get(argWhere { it.uri.contains("orgs/orgName/members") }) } doReturn
                    GitHubResponse(mock(), listOf(mockUser))
            on { get(argWhere { it.uri.contains("orgs/orgName/teams") }) } doReturn
                    GitHubResponse(mock(), listOf(mockTeam))
            on { get(argWhere { it.uri.contains("teams/1/members") }) } doReturn
                    GitHubResponse(mock(), listOf(userWithNullEmail))
            on { get(argWhere { it.uri.contains("emails") }) } doThrow RequestException(mock(), 404)
        }

        val sut = GithubApiClientAuthHelper(mockAppConfig, customMockGithubApiClient)

        sut.initialise("token")

        Assertions.assertThatThrownBy {
            sut.getUserEmail()
        }.isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("GitHub token must include scope user:email")
    }

}