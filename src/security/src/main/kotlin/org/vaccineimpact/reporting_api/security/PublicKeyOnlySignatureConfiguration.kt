package org.vaccineimpact.reporting_api.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.pac4j.core.exception.TechnicalException
import org.pac4j.core.util.CommonHelper
import org.pac4j.jwt.config.signature.AbstractSignatureConfiguration
import java.security.interfaces.RSAPublicKey
import java.util.*

/**
 * This is just a copy of RSASignatureConfiguration with the following changes:
 * 1. Translated to Kotlin
 * 2. Only requires a public key
 * 3. Does not implement `sign`
 */
class PublicKeyOnlySignatureConfiguration(val publicKey: RSAPublicKey)
    : AbstractSignatureConfiguration()
{
    init
    {
        algorithm = JWSAlgorithm.RS256;
    }

    val base64PublicKey by lazy {
        Base64.getEncoder().encodeToString(publicKey.encoded)
    }

    override fun internalInit()
    {
        CommonHelper.assertNotNull("algorithm", algorithm)
        if (!supports(this.algorithm)) {
            throw TechnicalException("Only the RS256, RS384, RS512, PS256, PS384 and PS512 algorithms are supported for RSA signature")
        }
    }

    override fun supports(algorithm: JWSAlgorithm?)
            = algorithm != null && RSASSAVerifier.SUPPORTED_ALGORITHMS.contains(algorithm)

    override fun sign(claims: JWTClaimsSet?): SignedJWT
    {
        throw NotImplementedError("This configuration can only verify tokens; it cannot sign them")
    }

    override fun verify(jwt: SignedJWT): Boolean
    {
        init()
        val verifier = RSASSAVerifier(publicKey)
        return jwt.verify(verifier)
    }

    override fun toString(): String
    {
        return CommonHelper.toString(this::class.java, "keys", "[protected]", "algorithm", algorithm)
    }
}