package org.vaccineimpact.reporting_api.tests.security

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.vaccineimpact.reporting_api.security.KeyHelper
import org.vaccineimpact.reporting_api.security.MontaguTokenAuthenticator
import org.vaccineimpact.reporting_api.security.InternalUser
import org.vaccineimpact.reporting_api.security.WebTokenHelper

class MontaguTokenAuthenticatorTests
{
    lateinit var helper: WebTokenHelper
    val fakeUser = InternalUser("tettusername", "user", "*/reports.read")

    val tokenIssuer = "tokenissuer"

    @Before
    fun createHelper()
    {
        helper = WebTokenHelper(KeyHelper.generateKeyPair(), tokenIssuer)
    }

    @Test
    fun `token is invalid if issuer is not expected`()
    {
        val url = "testurl"
        val token = helper.issuer.generateOnetimeActionToken(fakeUser, url)
        val credentials = TokenCredentials(token, "MontaguTests")

        val fakeContext = mock<WebContext>() {
            on(it.path) doReturn url
        }

        val sut = MontaguTokenAuthenticator(helper.verifier.signatureConfiguration, "differentissuer")

        assertThatThrownBy { sut.validate(credentials, fakeContext) }.isInstanceOf(CredentialsException::class.java)
    }

    @Test
    fun `global * attribute is added to user profile`()
    {
        val url = "testurl"
        val token = helper.issuer.generateOnetimeActionToken(fakeUser, url)
        val credentials = TokenCredentials(token, "MontaguTests")

        val fakeContext = mock<WebContext>() {
            on(it.path) doReturn url
        }

        val sut = MontaguTokenAuthenticator(helper.verifier.signatureConfiguration, helper.issuerName)

        sut.validate(credentials, fakeContext)

        assertThat(credentials.userProfile.getAttribute("url")).isEqualTo("*")
    }

}
