package org.vaccineimpact.reporting_api.security

import org.pac4j.core.profile.CommonProfile
import org.pac4j.jwt.config.signature.RSASignatureConfiguration
import org.pac4j.jwt.profile.JwtGenerator
import java.security.KeyPair
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.*
import org.vaccineimpact.reporting_api.db.AppConfig

open class TokenIssuer(keyPair: KeyPair, val issuer: String)
{
    val oneTimeLinkLifeSpan: Duration = Duration.ofMinutes(10)
    val signatureConfiguration = RSASignatureConfiguration(keyPair)

    val appConfig = AppConfig()
    val app_name = config["app.name"]
    val tokenLifeSpan = Duration.ofMinutes("token_expiry.minutes")

    val generator = JwtGenerator<CommonProfile>(signatureConfiguration)
    private val random = SecureRandom()

    open fun generateOnetimeActionToken(user: InternalUser, url: String): String
    {
        return generator.generate(oneTimeTokenClaims(user, url))
    }

    open fun generateBearerToken(user: InternalUser): String
    {
        return generator.generate(bearerTokenClaims(user))
    }

    private fun oneTimeTokenClaims(user: InternalUser, url: String): Map<String, Any>
    {
        return mapOf(
                "iss" to issuer,
                "sub" to oneTimeActionSubject,
                "exp" to getExpiry(oneTimeLinkLifeSpan),
                "permissions" to user.permissions,
                "roles" to user.roles,
                "url" to url,
                "nonce" to getNonce()
        )
    }

    private fun bearerTokenClaims(user: InternalUser): String
    {
        return mapOf(
                "sub" to user.username,
                "iss" to app_name,
                "exp" to getExpiry(tokenLifeSpan),
                "token_type" to "bearer"
        )
    }

    private fun getNonce(): String
    {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun getExpiry(duration: Duration)
    {
        Date.from(Instant.now().plus(duration))
    }

    companion object
    {
        val oneTimeActionSubject = "onetime_link"
    }
}