package org.vaccineimpact.reporting_api.security

import org.slf4j.LoggerFactory
import org.vaccineimpact.reporting_api.db.Config
import java.security.interfaces.RSAPublicKey
import java.util.*

class TokenVerifier(publicKey: RSAPublicKey, val expectedIssuer: String)
{
    val signatureConfiguration = PublicKeyOnlySignatureConfiguration(publicKey)
    private val logger = LoggerFactory.getLogger(TokenVerifier::class.java)

    init{
        logger.info("API is using this public key for auth token verification: "
                + Base64.getEncoder().encodeToString(KeyHelper.authPublicKey.encoded))
    }

    fun verify(token: String): Map<String, Any>
            = MontaguTokenAuthenticator(signatureConfiguration, expectedIssuer).validateTokenAndGetClaims(token)

    companion object{
        val authTokenVerifier = TokenVerifier(KeyHelper.authPublicKey, Config["token.issuer"])
    }

}