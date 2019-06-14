package org.vaccineimpact.orderlyweb.security.authentication

import org.pac4j.jwt.config.signature.RSASignatureConfiguration
import java.security.KeyPair

open class TokenVerifier(keyPair: KeyPair, val expectedIssuer: String)
{
    val signatureConfiguration = RSASignatureConfiguration(keyPair)
}