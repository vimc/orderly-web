package org.vaccineimpact.reporting_api.tests.security

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nimbusds.jwt.JWTParser
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import org.vaccineimpact.api.models.Scope
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.api.models.permissions.ReifiedRole
import org.vaccineimpact.reporting_api.security.*
import org.vaccineimpact.reporting_api.test_helpers.MontaguTests
import java.time.Instant
import java.util.*

class MontaguOnetimeTokenAuthenticatorTests : MontaguTests()
{
    lateinit var helper: WebTokenHelper
    val fakeUser = MontaguUser("tettusername", "user", "*/reports.read")

    val onetimeTokenIssuer = "onetimetokenissuer"

    @Before
    fun createHelper()
    {
        helper = WebTokenHelper(KeyHelper.generateKeyPair(), onetimeTokenIssuer)
    }

    @Test
    fun `can generate onetime token`()
    {
        val url = "testurl"
        val token = helper.issuer.generateOneTimeActionToken(fakeUser, url)

        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(token)) doReturn true
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration, helper.issuerName,
                fakeStore, url)

        assertThat(sut.validateToken(token)).isNotNull()

    }

    @Test
    fun `token fails validation when issuer is wrong`()
    {
        val url = "testurl"
        val claims = helper.issuer.claims(fakeUser, url)
        val badToken = helper.issuer.generator.generate(claims.plus("iss" to "unexpected.issuer"))


        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(badToken)) doReturn true
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration, helper.issuerName,
                fakeStore, url)

        assertThat(sut.validateToken(badToken)).isNull()
    }

    @Test
    fun `token fails validation when token is old`()
    {
        val url = "testurl"
        val claims = helper.issuer.claims(fakeUser, url)
        val badToken = helper.issuer.generator.generate(claims.plus("exp" to Date.from(Instant.now())))

        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(badToken)) doReturn true
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration,
                helper.issuerName, fakeStore, url)

        assertThat(sut.validateToken(badToken)).isNull()
    }

    @Test
    fun `token fails validation when token is signed by wrong key`()
    {
        val url = "testurl"
        val sauron = WebTokenHelper(KeyHelper.generateKeyPair(), onetimeTokenIssuer)
        val evilToken = sauron.issuer.generateOneTimeActionToken(fakeUser, url)

        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(evilToken)) doReturn true
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration,
                helper.issuerName,
                fakeStore,
                url)

        assertThat(sut.validateToken(evilToken)).isNull()
    }

    @Test
    fun `token fails validation when url is wrong`()
    {
        val url = "testurl"
        val badUrl = "badurl"
        val badToken = helper.issuer
                .generateOneTimeActionToken(fakeUser, url)

        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(badToken)) doReturn false
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration, helper.issuerName,
                fakeStore, badUrl)

        assertThat(sut.validateToken(badToken)).isNull()
    }

    @Test
    fun `token fails validation when not in the token store`()
    {
        val url = "testurl"
        val notInDbToken = helper.issuer
                .generateOneTimeActionToken(fakeUser, url)

        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(notInDbToken)) doReturn false
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration, helper.issuerName,
                fakeStore, url)

        assertThat(sut.validateToken(notInDbToken)).isNull()
    }
}