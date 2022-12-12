package org.vaccineimpact.orderlyweb.tests.security.authentication

import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.oauth.OAuth20Service
import com.nhaarman.mockito_kotlin.*
import org.pac4j.oauth.credentials.OAuth20Credentials
import org.junit.jupiter.api.Test
import org.pac4j.oauth.config.OAuth20Configuration
import org.vaccineimpact.orderlyweb.security.authentication.GithubOAuthAuthenticator
import org.vaccineimpact.orderlyweb.security.providers.GithubAuthHelper

class GithubOAuthAuthenticatorTests
{

    private val mockGithubAuthHelper = mock<GithubAuthHelper> {
    }

    private val mockAccessToken = mock<OAuth2AccessToken> {
        on { accessToken } doReturn "1234567"
    }

    private val mockService = mock<OAuth20Service> {
        on { getAccessToken(any<String>()) } doReturn mockAccessToken
    }

    private val mockConfiguration = mock<OAuth20Configuration> {
        on { buildService(any(), any())} doReturn mockService
    }


    private val mockCredentials = mock<OAuth20Credentials> {
        on { accessToken } doReturn mockAccessToken
    }

    @Test
    fun `can validate`()
    {
        val sut = GithubOAuthAuthenticator(mockConfiguration, mock(), mock(), mockGithubAuthHelper)

        sut.validate(mockCredentials, mock(), mock())

        verify(mockGithubAuthHelper).authenticate("1234567")
        verify(mockGithubAuthHelper).checkGitHubOrgAndTeamMembership()
    }
}