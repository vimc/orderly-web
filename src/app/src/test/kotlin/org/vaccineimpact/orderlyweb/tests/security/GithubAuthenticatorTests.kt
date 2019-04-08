package org.vaccineimpact.orderlyweb.tests.security

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eclipse.egit.github.core.User
import org.junit.Test
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.security.authentication.GithubAuthenticator
import org.vaccineimpact.orderlyweb.security.providers.GithubAuthHelper
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class GithubAuthenticatorTests : TeamcityTests()
{
    private val mockUser = User().apply {
        login = "user.name"
        email = null
        name = "full name"
    }

    private val mockGithubAuthHelper = mock<GithubAuthHelper> {
        on { getUser() } doReturn mockUser
        on { getUserEmail() } doReturn "email"
    }

    private val mockUserData = mock<UserRepository>()

    @Test
    fun `token validation fails if credentials are not supplied`()
    {
        val sut = GithubAuthenticator(mockUserData, mock(), mockGithubAuthHelper)

        assertThatThrownBy { sut.validate(null, mock()) }
                .isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("No credentials supplied")
    }

    @Test
    fun `token validation calls github auth helper`()
    {
        val sut = GithubAuthenticator(mockUserData, mock(), mockGithubAuthHelper)
        sut.validate(TokenCredentials("token"), mock())
        verify(mockGithubAuthHelper).authenticate("token")
        verify(mockGithubAuthHelper).checkGithubUserHasOrderlyWebAccess()
        verify(mockGithubAuthHelper).getUser()
        verify(mockGithubAuthHelper).getUserEmail()
    }

    @Test
    fun `url attribute is added to profile after successful validation`()
    {
        val sut = GithubAuthenticator(mockUserData, mock(), mockGithubAuthHelper)

        val credentials = TokenCredentials("token")
        sut.validate(credentials, mock())

        assertThat(credentials.userProfile.getAttribute("url")).isEqualTo("*")
    }

    @Test
    fun `profile id is set to email after successful validation`()
    {
        val sut = GithubAuthenticator(mockUserData, mock(), mockGithubAuthHelper)

        val credentials = TokenCredentials("token")
        sut.validate(credentials, mock())

        assertThat(credentials.userProfile.id).isEqualTo("email")
    }

    @Test
    fun `user is added to database successful validation`()
    {
        val sut = GithubAuthenticator(mockUserData, mock(), mockGithubAuthHelper)

        val credentials = TokenCredentials("token")
        sut.validate(credentials, mock())

        verify(mockUserData).addUser("email", "user.name", "full name", UserSource.GitHub)
    }

}