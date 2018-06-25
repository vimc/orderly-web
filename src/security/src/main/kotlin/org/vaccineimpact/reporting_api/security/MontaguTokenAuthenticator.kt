package org.vaccineimpact.reporting_api.security

import org.pac4j.core.profile.CommonProfile
import org.pac4j.jwt.config.signature.SignatureConfiguration
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator

abstract class MontaguTokenAuthenticator(signatureConfiguration: SignatureConfiguration,
                                         protected val expectedIssuer: String)
    : JwtAuthenticator(signatureConfiguration)
{
    override fun validateToken(compressedToken: String?): CommonProfile
    {
        return super.validateToken(inflate(compressedToken))
    }
}