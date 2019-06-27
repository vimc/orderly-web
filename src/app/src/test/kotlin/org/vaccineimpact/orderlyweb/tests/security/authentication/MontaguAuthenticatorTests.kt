package org.vaccineimpact.orderlyweb.tests.security.authentication

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.Test
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.security.providers.MontaguAPIClient
import org.vaccineimpact.orderlyweb.security.authentication.MontaguAuthenticator
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class MontaguAuthenticatorTests : TeamcityTests()
{
    private val fakeUserDetails = MontaguAPIClient.UserDetails("user@example.com",
            "test.user", "Test User")

    private val mockMontaguAPIClient = mock<MontaguAPIClient> {
        on { getUserDetails("token") } doReturn fakeUserDetails
    }

    private val mockUserRepo = mock<UserRepository>()

    @Test
    fun `token validation fails if credentials are not supplied`()
    {
        val sut = MontaguAuthenticator(mock(), mock())

        Assertions.assertThatThrownBy { sut.validate(null, mock()) }
                .isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("No credentials supplied")
    }

    @Test
    fun `token validation fails if token is blank`()
    {
        val sut = MontaguAuthenticator(mock(), mock())

        Assertions.assertThatThrownBy { sut.validate(TokenCredentials(""), mock()) }
                .isInstanceOf(CredentialsException::class.java)
                .hasMessageContaining("Token cannot be blank")
    }

    @Test
    fun `profile id is set to email after successful validation`()
    {
        val sut = MontaguAuthenticator(mock(), mockMontaguAPIClient)

        val credentials = TokenCredentials("token")
        sut.validate(credentials, mock())

        Assertions.assertThat(credentials.userProfile.id).isEqualTo("user@example.com")
    }

    @Test
    fun `user is added to database successful validation`()
    {
        val sut = MontaguAuthenticator(mockUserRepo, mockMontaguAPIClient)

        val credentials = TokenCredentials("token")
        sut.validate(credentials, mock())

        verify(mockUserRepo).addUser("user@example.com", "test.user", "Test User",
                UserSource.Montagu)
    }

    @Test
    fun `user can be saved even if name is null`()
    {
        val mockMontaguAPIClient = mock<MontaguAPIClient> {
            on { getUserDetails("token") } doReturn fakeUserDetails.copy(name = null)
        }
        val sut = MontaguAuthenticator(mockUserRepo, mockMontaguAPIClient)

        val credentials = TokenCredentials("token")
        sut.validate(credentials, mock())

        verify(mockUserRepo).addUser("user@example.com", "test.user", "",
                UserSource.Montagu)
    }

    @Test
    fun `adds url wildcard to profile`()
    {
        val mockMontaguAPIClient = mock<MontaguAPIClient> {
            on { getUserDetails("token") } doReturn fakeUserDetails.copy(name = null)
        }
        val sut = MontaguAuthenticator(mockUserRepo, mockMontaguAPIClient)

        val credentials = TokenCredentials("token")
        sut.validate(credentials, mock())

        assertThat(credentials.userProfile.getAttribute("url")).isEqualTo("*")
    }

}