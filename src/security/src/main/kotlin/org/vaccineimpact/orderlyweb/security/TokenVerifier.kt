package org.vaccineimpact.orderlyweb.security

import java.security.interfaces.RSAPublicKey

class TokenVerifier(publicKey: RSAPublicKey, val expectedIssuer: String)
{
    val signatureConfiguration = PublicKeyOnlySignatureConfiguration(publicKey)
}