package org.vaccineimpact.orderlyweb.tests.security

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.GitHubResponse
import org.junit.Test
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.vaccineimpact.orderlyweb.db.UserData
import org.vaccineimpact.orderlyweb.security.GithubAuthenticator
import org.vaccineimpact.orderlyweb.test_helpers.MontaguTests

class GithubAuthenticatorTests : MontaguTests()
{
    private val mockUser = User().apply {
        name = "user.name"
        email = "email"
    }

    private val mockGithubApiClient = mock<GitHubClient> {
        on { get(any()) } doReturn GitHubResponse(mock(), mockUser)
    }

    private val mockUserData = mock<UserData>()

    @Test
    fun `token validation fails if credentials are not supplied`()
    {
        val sut = GithubAuthenticator(mock(), mock())

        assertThatThrownBy { sut.validate(null, mock()) }
                .isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("No credentials supplied")
    }

    @Test
    fun `token validation fails if token is blank`()
    {
        val sut = GithubAuthenticator(mock(), mock())

        assertThatThrownBy { sut.validate(TokenCredentials("", ""), mock()) }
                .isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("Token cannot be blank")
    }

    @Test
    fun `url attribute is added to profile after successful validation`()
    {
        val sut = GithubAuthenticator(mock(), mockGithubApiClient)

        val credentials = TokenCredentials("token", "")
        sut.validate(credentials, mock())

        assertThat(credentials.userProfile.getAttribute("url")).isEqualTo("*")
    }

    @Test
    fun `profile id is set to email after successful validation`()
    {
        val sut = GithubAuthenticator(mock(), mockGithubApiClient)

        val credentials = TokenCredentials("token", "")
        sut.validate(credentials, mock())

        assertThat(credentials.userProfile.id).isEqualTo("email")
    }

    @Test
    fun `user is added to database successful validation`()
    {
        val sut = GithubAuthenticator(mockUserData, mockGithubApiClient)

        val credentials = TokenCredentials("token", "")
        sut.validate(credentials, mock())

        verify(mockUserData).addGithubUser("user.name", "email")
    }
}