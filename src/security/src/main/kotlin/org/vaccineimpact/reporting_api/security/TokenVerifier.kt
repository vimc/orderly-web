package org.vaccineimpact.reporting_api.security

import java.security.interfaces.RSAPublicKey

class TokenVerifier(publicKey: RSAPublicKey, val expectedIssuer: String)
{
    val signatureConfiguration = PublicKeyOnlySignatureConfiguration(publicKey)
}