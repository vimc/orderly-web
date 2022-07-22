package org.vaccineimpact.orderlyweb.security.authentication

import org.pac4j.jwt.config.signature.AbstractSignatureConfiguration
import org.pac4j.jwt.config.signature.RSASignatureConfiguration
import java.security.KeyPair

interface TokenVerifier
{
    val expectedIssuer: String
    val signatureConfiguration: AbstractSignatureConfiguration
}

class RSATokenVerifier(keyPair: KeyPair, override val expectedIssuer: String): TokenVerifier
{
    override val signatureConfiguration = RSASignatureConfiguration(keyPair)
}
