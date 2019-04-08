package org.vaccineimpact.orderlyweb.tests.security

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eclipse.egit.github.core.Team
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.GitHubResponse
import org.eclipse.egit.github.core.client.RequestException
import org.junit.Test
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.errors.BadConfigurationError
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.security.authentication.GithubAuthenticator
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class GithubAuthenticatorTests : TeamcityTests()
{
    private val mockUser = User().apply {
        login = "user.name"
        email = "email"
        name = "full name"
    }

    private val mockTeam = Team().apply {
        name = "teamName"
        id = 1
    }

    private val mockOrg = User().apply {
        login = "orgName"
    }

    private val mockAppConfig = mock<Config> {
        on { get("auth.github_org") } doReturn "orgName"
        on { get("auth.github_team") } doReturn "teamName"
    }

    private val mockGithubApiClient = mock<GitHubClient> {
        on { get(argWhere { it.uri.endsWith("user") }) } doReturn
                GitHubResponse(mock(), mockUser)
        on { get(argWhere { it.uri.contains("user/orgs") }) } doReturn
                GitHubResponse(mock(), listOf(mockOrg))
        on { get(argWhere { it.uri.contains("orgs/orgName/teams") }) } doReturn
                GitHubResponse(mock(), listOf(mockTeam))
        on { get(argWhere { it.uri.contains("teams/1/members") }) } doReturn
                GitHubResponse(mock(), listOf(mockUser))
    }

    private val mockUserData = mock<UserRepository>()

    @Test
    fun `token validation fails if credentials are not supplied`()
    {
        val sut = GithubAuthenticator(mock(), mock(), mockAppConfig)

        assertThatThrownBy { sut.validate(null, mock()) }
                .isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("No credentials supplied")
    }

    @Test
    fun `token validation fails if token is blank`()
    {
        val sut = GithubAuthenticator(mock(), mock(), mockAppConfig)

        assertThatThrownBy { sut.validate(TokenCredentials(""), mock()) }
                .isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("Token cannot be blank")
    }

    @Test
    fun `url attribute is added to profile after successful validation`()
    {
        val sut = GithubAuthenticator(mock(), mockGithubApiClient, mockAppConfig)

        val credentials = TokenCredentials("token")
        sut.validate(credentials, mock())

        assertThat(credentials.userProfile.getAttribute("url")).isEqualTo("*")
    }

    @Test
    fun `profile id is set to email after successful validation`()
    {
        val sut = GithubAuthenticator(mock(), mockGithubApiClient, mockAppConfig)

        val credentials = TokenCredentials("token")
        sut.validate(credentials, mock())

        assertThat(credentials.userProfile.id).isEqualTo("email")
    }

    @Test
    fun `user is added to database successful validation`()
    {
        val sut = GithubAuthenticator(mockUserData, mockGithubApiClient, mockAppConfig)

        val credentials = TokenCredentials("token")
        sut.validate(credentials, mock())

        verify(mockUserData).addUser("email", "user.name", "full name", UserSource.GitHub)
    }

    @Test
    fun `BadConfigurationError is thrown if team does not exist`()
    {
        val mockGithubApiClient = mock<GitHubClient> {
            on { get(argWhere { it.uri.endsWith("user") }) } doReturn
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

        val sut = GithubAuthenticator(mockUserData, mockGithubApiClient, mockAppConfig)

        val credentials = TokenCredentials("token")

        assertThatThrownBy {
            sut.validate(credentials, mock())
        }.isInstanceOf(BadConfigurationError::class.java)
                .hasMessageContaining("GitHub org orgName has no team called teamName")
    }

    @Test
    fun `CredentialsException is thrown if user does not belong to GitHub org`()
    {
        val mockGithubApiClient = mock<GitHubClient> {
            on { get(argWhere { it.uri.contains("user") }) } doReturn
                    GitHubResponse(mock(), mockUser)
            on { get(argWhere { it.uri.contains("orgName") }) } doReturn GitHubResponse(mock(),
                    listOf<User>())
        }

        val mockAppConfig = mock<Config> {
            on { get("auth.github_org") } doReturn "orgName"
        }

        val sut = GithubAuthenticator(mockUserData, mockGithubApiClient, mockAppConfig)

        val credentials = TokenCredentials("token")

        assertThatThrownBy {
            sut.validate(credentials, mock())
        }.isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("User is not a member of GitHub org orgName or token does not include read:org scope")
    }

    @Test
    fun `CredentialsException is thrown if user does not belong to GitHub team`()
    {
        val mockGithubApiClient = mock<GitHubClient> {
            on { get(argWhere { it.uri.endsWith("user") }) } doReturn
                    GitHubResponse(mock(), mockUser)
            on { get(argWhere { it.uri.contains("user/orgs") }) } doReturn
                    GitHubResponse(mock(), listOf(mockOrg))
            on { get(argWhere { it.uri.contains("orgs/orgName/teams") }) } doReturn
                    GitHubResponse(mock(), listOf(mockTeam))
            on { get(argWhere { it.uri.contains("teams/1/members") }) } doReturn
                    GitHubResponse(mock(), listOf<User>())
        }

        val sut = GithubAuthenticator(mockUserData, mockGithubApiClient, mockAppConfig)

        val credentials = TokenCredentials("token")

        assertThatThrownBy {
            sut.validate(credentials, mock())
        }.isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("User is not a member of GitHub team teamName")
    }

    @Test
    fun `CredentialsException is thrown if token does not have email scope`()
    {
        val userWithNullEmail = mockUser.apply { email = null }
        val mockGithubApiClient = mock<GitHubClient> {
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

        val sut = GithubAuthenticator(mockUserData, mockGithubApiClient, mockAppConfig)

        val credentials = TokenCredentials("token")

        assertThatThrownBy {
            sut.validate(credentials, mock())
        }.isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("GitHub token must include scope user:email")
    }

}