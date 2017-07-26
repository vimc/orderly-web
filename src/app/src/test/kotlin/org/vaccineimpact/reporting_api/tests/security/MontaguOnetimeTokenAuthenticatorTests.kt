package org.vaccineimpact.reporting_api.tests.security

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
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
    val properties = UserProperties(
            username = "test.user",
            name = "Test User",
            email = "test@example.com",
            passwordHash = "",
            lastLoggedIn = null
    )
    val roles = listOf(
            ReifiedRole("roleA", Scope.Global()),
            ReifiedRole("roleB", Scope.Specific("prefix", "id"))
    )
    val permissions = listOf(
            ReifiedPermission("p1", Scope.Global()),
            ReifiedPermission("p2", Scope.Specific("prefix", "id"))
    )

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
        val token = helper.issuer.generateOneTimeActionToken(MontaguUser(properties, roles, permissions), url)
  //      val claims = helper.verifier.verify(token)

//        assertThat(claims["iss"]).isEqualTo(onetimeTokenIssuer)
//        assertThat(claims["sub"]).isEqualTo(TokenIssuer.oneTimeActionSubject)
//        assertThat(claims["exp"]).isInstanceOf(Date::class.java)
//        assertThat(claims["roles"]).isEqualTo("*/roleA,prefix:id/roleB")
//        assertThat(claims["permissions"]).isEqualTo("*/p1,prefix:id/p2")
//        assertThat(claims["url"]).isEqualTo(url)
    }

    @Test
    fun `token fails validation when issuer is wrong`()
    {
        val url = "testurl"
        val claims = helper.issuer.claims(MontaguUser(properties, roles, permissions), url)
        val badToken = helper.issuer.generator.generate(claims.plus("iss" to "unexpected.issuer"))


        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(badToken)) doReturn true
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration, helper.issuerName,
                fakeStore, url)

        assertThat(sut.validateToken(badToken)).isNull()
 //       assertThatThrownBy { helper.verifier.verify(badToken) }
    }

    @Test
    fun `token fails validation when token is old`()
    {
        val url = "testurl"
        val claims = helper.issuer.claims(MontaguUser(properties, roles, permissions), url)
        val badToken = helper.issuer.generator.generate(claims.plus("exp" to Date.from(Instant.now())))

        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(badToken)) doReturn true
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration,
                helper.issuerName, fakeStore, url)

        assertThat(sut.validateToken(badToken)).isNull()
  //      assertThatThrownBy { helper.verifier.verify(badToken) }
    }

    @Test
    fun `token fails validation when token is signed by wrong key`()
    {
        val url = "testurl"
        val sauron = WebTokenHelper(KeyHelper.generateKeyPair(), onetimeTokenIssuer)
        val evilToken = sauron.issuer.generateOneTimeActionToken(MontaguUser(properties, roles, permissions), url)

        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(evilToken)) doReturn true
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration,
                helper.issuerName,
                fakeStore,
                url)

        assertThat(sut.validateToken(evilToken)).isNull()
   //     assertThatThrownBy { helper.verifier.verify(evilToken) }
    }

    @Test
    fun `token fails validation when url is wrong`()
    {
        val url = "testurl"
        val badUrl = "badurl"
        val badToken = helper.issuer
                .generateOneTimeActionToken(MontaguUser(properties, roles, permissions), url)

        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(badToken)) doReturn false
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration, helper.issuerName,
                fakeStore, badUrl)

        assertThat(sut.validateToken(badToken)).isNull()
     //   assertThatThrownBy { helper.verifier.verify(badToken) }
    }

    @Test
    fun `token fails validation when not in the token store`()
    {
        val url = "testurl"
        val notInDbToken = helper.issuer
                .generateOneTimeActionToken(MontaguUser(properties, roles, permissions), url)

        val fakeStore = mock<OnetimeTokenStore>() {
            on(it.validateOneTimeToken(notInDbToken)) doReturn false
        }

        val sut = MontaguOnetimeTokenAuthenticator(helper.verifier.signatureConfiguration, helper.issuerName,
                fakeStore, url)

        assertThat(sut.validateToken(notInDbToken)).isNull()
    }
}