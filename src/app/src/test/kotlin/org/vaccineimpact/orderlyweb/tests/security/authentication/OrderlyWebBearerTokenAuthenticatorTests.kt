package org.vaccineimpact.orderlyweb.tests.security.authentication

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.vaccineimpact.orderlyweb.security.issuing.KeyHelper
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebBearerTokenAuthenticator
import org.vaccineimpact.orderlyweb.security.WebTokenHelper

class OrderlyWebBearerTokenAuthenticatorTests
{
    lateinit var helper: WebTokenHelper
    val fakeUserEmail = "user@email.com"
    val tokenIssuer = "tokenissuer"

    @BeforeEach
    fun createHelper()
    {
        helper = WebTokenHelper(KeyHelper.generateKeyPair(), tokenIssuer)
    }

    @Test
    fun `token is invalid if issuer is not expected`()
    {
        val url = "testurl"
        val token = helper.issuer.generateBearerToken(fakeUserEmail)

        val credentials = TokenCredentials(token)

        val fakeContext = mock<WebContext>() {
            on(it.path) doReturn url
        }

        val sut = OrderlyWebBearerTokenAuthenticator(helper.verifier.signatureConfiguration, "differentissuer")

        assertThatThrownBy { sut.validate(credentials, fakeContext, mock()) }.isInstanceOf(CredentialsException::class.java)
    }

    @Test
    fun `global * attribute is added to user profile`()
    {
        val url = "testurl"
        val token = helper.issuer.generateBearerToken(fakeUserEmail)

        val credentials = TokenCredentials(token)

        val fakeContext = mock<WebContext>() {
            on(it.path) doReturn url
        }

        val sut = OrderlyWebBearerTokenAuthenticator(helper.verifier.signatureConfiguration, helper.issuerName)

        sut.validate(credentials, fakeContext, mock())

        assertThat(credentials.userProfile.getAttribute("url")).isEqualTo("*")
    }

}
