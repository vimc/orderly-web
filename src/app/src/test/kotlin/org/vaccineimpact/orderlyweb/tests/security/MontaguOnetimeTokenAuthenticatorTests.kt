package org.vaccineimpact.orderlyweb.tests.security

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.vaccineimpact.orderlyweb.security.*
import org.vaccineimpact.orderlyweb.test_helpers.MontaguTests
import java.time.Instant
import java.util.*

class MontaguOnetimeTokenAuthenticatorTests : MontaguTests()
{
    lateinit var helper: WebTokenHelper
    val fakeUser = InternalUser("tettusername", "user", "*/reports.read")

    val onetimeTokenIssuer = "onetimetokenissuer"

    @Before
    fun createHelper()
    {
        helper = WebTokenHelper(KeyHelper.generateKeyPair(), onetimeTokenIssuer)
    }

    @Test
    fun `can validate onetime token`()
    {
        val url = "testurl"
        val token = helper.issuer.generateOnetimeActionToken(fakeUser, url)
        val credentials = TokenCredentials(token, "MontaguTests")

        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(token)) doReturn true
        }

        val fakeContext = mock<WebContext>() {
            on(it.path) doReturn url
        }


        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration, helper.issuerName,
                fakeStore)

        sut.validate(credentials, fakeContext)

    }

    @Test
    fun `token fails validation when issuer is wrong`()
    {
        val url = "testurl"
        val claims = helper.issuer.claims(fakeUser, url)
        val badToken = helper.issuer.generator.generate(claims.plus("iss" to "unexpected.issuer"))
        val credentials = TokenCredentials(badToken, "MontaguTests")


        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(badToken)) doReturn true
        }

        val fakeContext = mock<WebContext>() {
            on(it.path) doReturn url
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration, helper.issuerName,
                fakeStore)

        assertThatThrownBy { sut.validate(credentials, fakeContext) }.isInstanceOf(CredentialsException::class.java)
    }

    @Test
    fun `token fails validation when token is old`()
    {
        val url = "testurl"
        val claims = helper.issuer.claims(fakeUser, url)
        val badToken = helper.issuer.generator.generate(claims.plus("exp" to Date.from(Instant.now())))
        val credentials = TokenCredentials(badToken, "MontaguTests")

        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(badToken)) doReturn true
        }

        val fakeContext = mock<WebContext>() {
            on(it.path) doReturn url
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration,
                helper.issuerName, fakeStore)

        sut.validate(credentials, fakeContext)
        assertThat(credentials.userProfile).isNull()
    }

    @Test
    fun `token fails validation when token is signed by wrong key`()
    {
        val url = "testurl"
        val sauron = WebTokenHelper(KeyHelper.generateKeyPair(), onetimeTokenIssuer)
        val evilToken = sauron.issuer.generateOnetimeActionToken(fakeUser, url)
        val credentials = TokenCredentials(evilToken, "MontaguTests")

        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(evilToken)) doReturn true
        }

        val fakeContext = mock<WebContext>() {
            on(it.path) doReturn url
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration,
                helper.issuerName,
                fakeStore)

        assertThatThrownBy { sut.validate(credentials, fakeContext) }.isInstanceOf(CredentialsException::class.java)
    }

    @Test
    fun `token fails validation when url is not request path`()
    {
        val badToken = helper.issuer
                .generateOnetimeActionToken(fakeUser, "")

        val credentials = TokenCredentials(badToken, "MontaguTests")

        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(badToken)) doReturn true
        }

        val fakeContext = mock<WebContext>() {
            on(it.path) doReturn "url"
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration, helper.issuerName,
                fakeStore)

        assertThatThrownBy { sut.validate(credentials, fakeContext) }.isInstanceOf(CredentialsException::class.java)
    }

    @Test
    fun `token fails validation when not in the token store`()
    {
        val url = "testurl"
        val notInDbToken = helper.issuer
                .generateOnetimeActionToken(fakeUser, url)

        val credentials = TokenCredentials(notInDbToken, "MontaguTests")

        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(notInDbToken)) doReturn false
        }

        val fakeContext = mock<WebContext>() {
            on(it.path) doReturn url
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration, helper.issuerName,
                fakeStore)

        assertThatThrownBy { sut.validate(credentials, fakeContext) }.isInstanceOf(CredentialsException::class.java)
    }
}