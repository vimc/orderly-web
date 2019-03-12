package org.vaccineimpact.reporting_api.tests.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.nimbusds.jwt.JWTParser
import org.vaccineimpact.reporting_api.security.InternalUser
import org.vaccineimpact.reporting_api.security.KeyHelper
import org.vaccineimpact.reporting_api.security.TokenIssuer
import org.vaccineimpact.reporting_api.test_helpers.MontaguTests
import java.time.Duration
import java.time.Instant
import java.util.*

class TokenIssuerTests : MontaguTests()
{
    private val keyPair= KeyHelper.generateKeyPair()
    private val user = InternalUser("testusername", "testroles", "testperms")

    @Test
    fun `can generate onetime token`()
    {
        val sut = TokenIssuer(keyPair, "testIssuer")

        val result = sut.generateOnetimeActionToken(user, "/test")
        val jwt = JWTParser.parse(result)
        val claims = jwt.jwtClaimsSet.claims
        assertThat(claims["iss"]).isEqualTo("testIssuer")
        assertThat(claims["sub"]).isEqualTo("onetime_link")
        assertThat(claims["permissions"]).isEqualTo("testperms")
        assertThat(claims["roles"]).isEqualTo("testroles")
        assertThat(claims["url"]).isEqualTo("/test")
        assertThat(claims["roles"]).isEqualTo("testroles")
        assertThat(claims["nonce"]).isNotNull()

        val exp = claims["exp"] as Date
        assertThat(exp).isInSameMinuteWindowAs(Date.from(Instant.now().plus(Duration.ofMinutes(10))))
    }

    @Test
    fun `can generate bearer token`()
    {
        val sut = TokenIssuer(keyPair, "testIssuer")

        val result = sut.generateBearerToken(user)
        val jwt = JWTParser.parse(result)

        val claims = jwt.jwtClaimsSet.claims
        assertThat(claims["iss"]).isEqualTo("orderlyweb")
        assertThat(claims["sub"]).isEqualTo("testusername")
        assertThat(claims["token_type"]).isEqualTo("bearer")

        val exp = claims["exp"] as Date
        assertThat(exp).isInSameMinuteWindowAs(Date.from(Instant.now().plus(Duration.ofHours(1))))
    }


}