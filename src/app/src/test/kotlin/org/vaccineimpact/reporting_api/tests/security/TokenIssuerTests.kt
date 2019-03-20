package org.vaccineimpact.reporting_api.tests.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.nimbusds.jwt.JWTParser
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.vaccineimpact.orderlyweb.security.issuing.KeyHelper
import org.vaccineimpact.orderlyweb.security.issuing.TokenIssuer
import org.vaccineimpact.orderlyweb.test_helpers.MontaguTests
import java.time.Duration
import java.time.Instant
import java.util.*

class TokenIssuerTests : MontaguTests()
{
    private val keyPair= KeyHelper.generateKeyPair()
    private val userEmail = "test@email.com"

    @Test
    fun `can generate onetime token`()
    {
        val sut = TokenIssuer(keyPair, "testIssuer")

        val result = sut.generateOnetimeActionToken(userEmail, "/test")

        // Check that valid token has been generated
        JwtAuthenticator(sut.signatureConfiguration).validateToken(result)

        // Check the token has expected claims
        val jwt = JWTParser.parse(result)
        val claims = jwt.jwtClaimsSet.claims
        assertThat(claims["iss"]).isEqualTo("testIssuer")
        assertThat(claims["sub"]).isEqualTo("onetime_link")
        assertThat(claims["url"]).isEqualTo("/test")
        assertThat(claims["nonce"]).isNotNull()

        val exp = claims["exp"] as Date
        assertThat(exp).isInSameMinuteWindowAs(Date.from(Instant.now().plus(Duration.ofMinutes(10))))
    }

    @Test
    fun `can generate bearer token`()
    {
        val sut = TokenIssuer(keyPair, "testIssuer")

        val result = sut.generateBearerToken(userEmail)

        // Check that valid token has been generated
        JwtAuthenticator(sut.signatureConfiguration).validateToken(result)

        // Check the token has expected claims
        val jwt = JWTParser.parse(result)
        val claims = jwt.jwtClaimsSet.claims
        assertThat(claims["iss"]).isEqualTo("testIssuer")
        assertThat(claims["sub"]).isEqualTo("testusername")
        assertThat(claims["token_type"]).isEqualTo("bearer")

        val exp = claims["exp"] as Date
        assertThat(exp).isInSameMinuteWindowAs(Date.from(Instant.now().plus(Duration.ofHours(1))))
    }
}