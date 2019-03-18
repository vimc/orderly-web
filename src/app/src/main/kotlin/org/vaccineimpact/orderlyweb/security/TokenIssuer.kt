package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.profile.CommonProfile
import org.pac4j.jwt.config.signature.RSASignatureConfiguration
import org.pac4j.jwt.profile.JwtGenerator
import java.security.KeyPair
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.*
import org.vaccineimpact.orderlyweb.db.AppConfig

open class TokenIssuer(keyPair: KeyPair, val issuer: String)
{
    val oneTimeLinkLifeSpan: Duration = Duration.ofMinutes(10)
    val signatureConfiguration = RSASignatureConfiguration(keyPair)

    val tokenLifeSpan = Duration.ofMinutes(AppConfig()["token_expiry.minutes"].toLong())

    val generator = JwtGenerator<CommonProfile>(signatureConfiguration)
    private val random = SecureRandom()

    open fun generateOnetimeActionToken(user: InternalUser, url: String): String
    {
        return generator.generate(onetimeTokenClaims(user, url))
    }

    open fun generateBearerToken(emailId: String): String
    {
        return generator.generate(bearerTokenClaims(emailId))
    }

    fun onetimeTokenClaims(user: InternalUser, url: String): Map<String, Any>
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

    fun bearerTokenClaims(emailId: String): Map<String, Any>
    {
        return mapOf(
                "sub" to emailId,
                "iss" to issuer,
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

    private fun getExpiry(duration: Duration): Date
    {
        return Date.from(Instant.now().plus(duration))
    }

    companion object
    {
        val oneTimeActionSubject = "onetime_link"
    }
}