package org.vaccineimpact.orderlyweb.tests.security

import com.nhaarman.mockito_kotlin.*
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertSame
import org.assertj.core.api.Assertions
import org.junit.Test
import org.pac4j.core.exception.CredentialsException
import org.kohsuke.github.*
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.BadConfigurationError
import org.vaccineimpact.orderlyweb.security.providers.GithubApiClientAuthHelper

class GithubApiClientAuthHelperTests
{
    private val token = "token"
    private val orgName = "orgName"
    private val teamName = "teamName"

    private val orgNameWithoutUser = "orgNameWithoutUser"
    private val teamNameWithoutUser = "teamNameWithoutUser"

    private val mockTeam = mock<GHTeam>()
    private val mockTeamWithoutUser = mock<GHTeam>()

    private val mockOrg = mock<GHOrganization>{
        on { name } doReturn orgName
        on { teams } doReturn mapOf(teamName to mockTeam, teamNameWithoutUser to mockTeamWithoutUser)
    }

    private val mockOrgWithoutUser = mock<GHOrganization>()

    private val mockEmail = mock<GHEmail>{
        on { email } doReturn "privateEmail"
    }

    private val mockUser = mock<GHMyself>{
        on { isMemberOf(mockOrg) } doReturn true
        on { isMemberOf(mockTeam) } doReturn true
        on { isMemberOf(mockOrgWithoutUser) } doReturn false
        on { isMemberOf(mockTeamWithoutUser) } doReturn false
        on { emails2 } doReturn listOf(mockEmail)
    }

    private val mockGithub = mock<GitHub> {
        on { myself } doReturn mockUser
        on { getOrganization(orgName) } doReturn mockOrg
        on { getOrganization(orgNameWithoutUser) } doReturn mockOrgWithoutUser
    }

    private fun getGitHubBuilder(github: GitHub): GitHubBuilder
    {
        val mockBuilderResult = mock<GitHubBuilder> {
            on { build() } doReturn github
        }

        return mock {
            on { withOAuthToken(token) } doReturn mockBuilderResult
        }
    }

    private val mockGithubBuilder = getGitHubBuilder(mockGithub)

    private val mockAppConfig = mock<Config> {
        on { get("auth.github_org") } doReturn orgName
        on { get("auth.github_team") } doReturn teamName
    }

    @Test
    fun `authenticate fails if token is blank`()
    {
        val sut = GithubApiClientAuthHelper(mock(), mock())

        Assertions.assertThatThrownBy { sut.authenticate("") }
                .isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("Token cannot be blank")
    }

    @Test
    fun `can authenticate`()
    {
        val ghBuilder = mockGithubBuilder
        val sut = GithubApiClientAuthHelper(mockAppConfig, ghBuilder)
        sut.authenticate(token)
        verify(ghBuilder).withOAuthToken(token)
    }

    @Test
    fun `checkGithubUserCanAuthenticate succeeds when user is member of allowed org and team`()
    {
        //default mock values should succeed
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubBuilder)
        sut.authenticate(token)
        sut.checkGitHubOrgAndTeamMembership()
    }

    @Test
    fun `can getUser`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubBuilder)
        sut.authenticate(token)
        val result = sut.getUser()
        assertSame(mockUser, result)
    }

    @Test
    fun `can getUserEmail when email is private`()
    {
        //defaults should get the sut to find null email in user so fetch details from client
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubBuilder)
        sut.authenticate(token)
        val result = sut.getUserEmail()
        assertEquals("privateEmail", result)
    }

    @Test
    fun `can getUserEmail when email is public`()
    {
        val customMockUser = mock<GHMyself>{
            on { email } doReturn "publicEmail"
        }

        val customMockGithub = mock<GitHub> {
            on { myself } doReturn customMockUser
        }

        val sut = GithubApiClientAuthHelper(mockAppConfig, getGitHubBuilder(customMockGithub))
        sut.authenticate(token)
        val result = sut.getUserEmail()
        assertEquals("publicEmail", result)
    }

    @Test
    fun `on checkGithubOrgAndTeamMembership IllegalStateException thrown if not authenticated`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubBuilder)
        Assertions.assertThatThrownBy {
            sut.checkGitHubOrgAndTeamMembership()
        }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("User has not been authenticated")
    }

    @Test
    fun `on getUser, IllegalStateException thrown if not authenticated`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubBuilder)
        Assertions.assertThatThrownBy {
            sut.getUser()
        }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("User has not been authenticated")
    }

    @Test
    fun `on getUserEmail, IllegalStateException thrown if not authenticated`()
    {
        val sut = GithubApiClientAuthHelper(mockAppConfig, mockGithubBuilder)
        Assertions.assertThatThrownBy {
            sut.getUserEmail()
        }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("User has not been authenticated")
    }

    @Test
    fun `on check team membership, BadConfigurationError is thrown if team is not in org`()
    {
        val customMockAppConfig = mock<Config> {
            on { get("auth.github_org") } doReturn orgName
            on { get("auth.github_team") } doReturn "teamNotInOrg"
        }

        val sut = GithubApiClientAuthHelper(customMockAppConfig, mockGithubBuilder)

        sut.authenticate("token")

        Assertions.assertThatThrownBy {
            sut.checkGitHubOrgAndTeamMembership()
        }.isInstanceOf(BadConfigurationError::class.java)
                .hasMessageContaining("GitHub org orgName has no team called teamNotInOrg")
    }

    @Test
    fun `on check org membership, CredentialsException is thrown if user does not belong to GitHub org`()
    {
        val customMockAppConfig = mock<Config> {
            on { get("auth.github_org") } doReturn orgNameWithoutUser
            on { get("auth.github_team") } doReturn teamName
        }

        val sut = GithubApiClientAuthHelper(customMockAppConfig, mockGithubBuilder)

        sut.authenticate("token")

        Assertions.assertThatThrownBy {
            sut.checkGitHubOrgAndTeamMembership()
        }.isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("User is not a member of GitHub org orgName")
    }

    @Test
    fun `on check team membership, CredentialsException is thrown if user does not belong to GitHub team`()
    {
        val customMockAppConfig = mock<Config> {
            on { get("auth.github_org") } doReturn orgName
            on { get("auth.github_team") } doReturn teamNameWithoutUser
        }

        val sut = GithubApiClientAuthHelper(customMockAppConfig, mockGithubBuilder)

        sut.authenticate("token")

        Assertions.assertThatThrownBy {
            sut.checkGitHubOrgAndTeamMembership()
        }.isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("User is not a member of GitHub team teamName")
    }

    @Test
    fun `on getUserEmail, CredentialsException is thrown if email is not public and token does not have email scope`()
    {
        val customMockUser = mock<GHMyself>{
            on { emails2 } doThrow GHFileNotFoundException()
        }

        val customMockGithub = mock<GitHub> {
            on { myself } doReturn customMockUser
        }

        val sut = GithubApiClientAuthHelper(mockAppConfig, getGitHubBuilder(customMockGithub))

        sut.authenticate("token")

        Assertions.assertThatThrownBy {
            sut.getUserEmail()
        }.isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("GitHub token must include scope user:email")
    }
}
