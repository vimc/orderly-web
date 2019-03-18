package org.vaccineimpact.orderlyweb.tests.security

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.junit.Test
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.vaccineimpact.orderlyweb.security.GithubAuthenticator
import org.vaccineimpact.orderlyweb.test_helpers.MontaguTests

class GithubAuthenticatorTests : MontaguTests()
{
    @Test
    fun `token validation fails if credentials are not supplied`()
    {
        val sut = GithubAuthenticator()

        Assertions.assertThatThrownBy { sut.validate(null, mock()) }
                .isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("No credentials supplied")
    }

    @Test
    fun `token validation fails if token is blank`()
    {
        val sut = GithubAuthenticator()

        Assertions.assertThatThrownBy { sut.validate(TokenCredentials("", ""), mock()) }
                .isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("Token cannot be blank")
    }

    @Test
    fun `url attribute is added to profile after successful validation`()
    {
        val sut = GithubAuthenticator()

        val credentials = TokenCredentials("token", "")
        sut.validate(credentials, mock())

        Assertions.assertThat(credentials.userProfile.getAttribute("url")).isEqualTo("*")
    }


    @Test
    fun `username is added to profile after successful validation`()
    {
        val sut = GithubAuthenticator()

        val credentials = TokenCredentials("token", "")
        sut.validate(credentials, mock())

        Assertions.assertThat(credentials.userProfile.username).isEqualTo("user.name")
    }
}