package org.vaccineimpact.orderlyweb.security.issuing

import org.pac4j.jwt.config.signature.RSASignatureConfiguration
import org.pac4j.jwt.profile.JwtGenerator
import org.vaccineimpact.orderlyweb.db.AppConfig
import java.security.KeyPair
import java.time.Duration
import java.time.Instant
import java.util.*

const val NONCE_SIZE = 32

open class TokenIssuer(keyPair: KeyPair, val issuer: String)
{
    val signatureConfiguration = RSASignatureConfiguration(keyPair)

    val tokenLifeSpan = Duration.ofMinutes(AppConfig()["token_expiry.minutes"].toLong())

    val generator = JwtGenerator(signatureConfiguration)

    open fun generateBearerToken(emailAddress: String): String
    {
        return generator.generate(bearerTokenClaims(emailAddress))
    }

    fun bearerTokenClaims(emailAddress: String): Map<String, Any>
    {
        return mapOf(
                "sub" to emailAddress,
                "iss" to issuer,
                "exp" to getExpiry(tokenLifeSpan),
                "token_type" to "bearer"
        )
    }

    private fun getExpiry(duration: Duration): Date
    {
        return Date.from(Instant.now().plus(duration))
    }
}
