package org.vaccineimpact.orderlyweb.tests.security

import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.oauth.OAuth20Service
import com.github.scribejava.core.model.Response
import com.nhaarman.mockito_kotlin.*
import org.eclipse.egit.github.core.User
import org.junit.Test
import org.pac4j.oauth.config.OAuth20Configuration
import org.pac4j.oauth.credentials.OAuth20Credentials
import org.pac4j.oauth.profile.definition.OAuthProfileDefinition
import org.pac4j.oauth.profile.github.GitHubProfile
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.security.authentication.GithubOAuthProfileCreator
import org.vaccineimpact.orderlyweb.security.providers.GithubAuthHelper
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests


class GithubOAuthProfileCreatorTests : TeamcityTests()
{
    private val mockUser = User().apply {
        login = "user.name"
        email = null
        name = "full name"
    }

    private val mockResponse = mock<Response> {
        on { code } doReturn 200
        on { body } doReturn ""
    }

    private val mockService = mock<OAuth20Service> {
        on { execute(any()) } doReturn mockResponse
    }

    private val mockProfileDefinition = mock<OAuthProfileDefinition<GitHubProfile, OAuth2AccessToken, OAuth20Configuration>>() {
        on {extractUserProfile(any())} doReturn mock<GitHubProfile>()
    }

    private val mockConfiguration = mock<OAuth20Configuration> {
        on { buildService(any(), any(), eq(null) )} doReturn mockService
        on { getProfileDefinition() } doReturn mockProfileDefinition
    }

    private val mockGithubAuthHelper = mock<GithubAuthHelper> {
        on { getUser() } doReturn mockUser
        on { getUserEmail() } doReturn "email"
    }

    private val mockAccessToken = mock<OAuth2AccessToken> {
        on { accessToken } doReturn "1234567"
    }

    private val mockCredentials = mock<OAuth20Credentials> {
       on { accessToken } doReturn mockAccessToken
    }

    private val mockUserRepo = mock<UserRepository> {

    }

    @Test
    fun `can create`()
    {
        val sut = GithubOAuthProfileCreator(mockConfiguration, mock(), mockUserRepo, mock(), mockGithubAuthHelper)

        sut.create(mockCredentials, mock()) //This calls the overridden retrieveUserProfileFromToken method

        verify(mockGithubAuthHelper).authenticate("1234567")
        verify(mockGithubAuthHelper).getUser()
        verify(mockGithubAuthHelper).getUserEmail()
        verify(mockUserRepo).addUser("email", "user.name", "full name", UserSource.GitHub)
    }

}