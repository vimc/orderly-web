package org.vaccineimpact.reporting_api.security

import org.pac4j.core.profile.CommonProfile
import org.pac4j.jwt.config.signature.RSASignatureConfiguration
import org.pac4j.jwt.profile.JwtGenerator
import org.vaccineimpact.reporting_api.db.Config
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.*

open class WebTokenHelper
{
    val lifeSpan: Duration = Duration.ofSeconds(Config["token.lifespan"].toLong())
    val oneTimeLinkLifeSpan: Duration = Duration.ofMinutes(10)
    private val keyPair = generateKeyPair()
    val issuer = Config["token.issuer"]
    val signatureConfiguration = RSASignatureConfiguration(keyPair)
    val generator = JwtGenerator<CommonProfile>(signatureConfiguration)
    private val random = SecureRandom()

    //val publicKey: String = Base64.getUrlEncoder().encodeToString(keyPair.public.encoded)

    fun generateToken(user: MontaguUser): String
    {
        return generator.generate(claims(user))
    }
    open fun generateOneTimeActionToken(action: String, params: Map<String, String>): String
    {
        return generator.generate(mapOf(
                "iss" to issuer,
                "sub" to oneTimeActionSubject,
                "exp" to Date.from(Instant.now().plus(oneTimeLinkLifeSpan)),
                "action" to action,
                "payload" to params.map { "${it.key}=${it.value}" }.joinToString("&"),
                "nonce" to getNonce()
        ))
    }

    fun claims(user: MontaguUser): Map<String, Any>
    {
        return mapOf(
                "iss" to issuer,
                "sub" to user.username,
                "exp" to Date.from(Instant.now().plus(lifeSpan)),
                "permissions" to user.permissions.joinToString(","),
                "roles" to user.roles.joinToString(",")
        )
    }

    open fun verify(token: String): Map<String, Any> = MontaguTokenAuthenticator(this).validateTokenAndGetClaims(token)

    private fun getNonce(): String
    {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun generateKeyPair(): KeyPair
    {
        val generator = KeyPairGenerator.getInstance("RSA").apply {
            initialize(1024)
        }
        return generator.generateKeyPair()
    }

    companion object
    {
        val oneTimeActionSubject = "onetime_link"
    }
}