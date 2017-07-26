package org.vaccineimpact.reporting_api.security

import org.pac4j.core.profile.CommonProfile
import org.pac4j.jwt.config.signature.RSASignatureConfiguration
import org.pac4j.jwt.profile.JwtGenerator
import java.security.KeyPair
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.*

/**
 * We don't issue auth tokens - we rely on the main Montagu API for that.
 * However, we still need to be able to generate tokens for other things:
 * In this case, one time action tokens.
 */
open class TokenIssuer(keyPair: KeyPair, val issuer: String) {
    val oneTimeLinkLifeSpan: Duration = Duration.ofMinutes(10)
    val signatureConfiguration = RSASignatureConfiguration(keyPair)
    val generator = JwtGenerator<CommonProfile>(signatureConfiguration)
    private val random = SecureRandom()

    open fun generateOneTimeActionToken(action: String, params: Map<String, String>): String {
        return generator.generate(mapOf(
                "iss" to issuer,
                "sub" to oneTimeActionSubject,
                "exp" to Date.from(Instant.now().plus(oneTimeLinkLifeSpan)),
                "action" to action,
                "payload" to params.map { "${it.key}=${it.value}" }.joinToString("&"),
                "nonce" to getNonce()
        ))
    }

    private fun getNonce(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    companion object {
        val oneTimeActionSubject = "onetime_link"
    }
}