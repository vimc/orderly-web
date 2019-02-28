package org.vaccineimpact.orderlyweb.security

import com.nimbusds.jwt.JWT
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.pac4j.jwt.config.signature.SignatureConfiguration

open class MontaguBearerTokenAuthenticator(signatureConfiguration: SignatureConfiguration, expectedIssuer: String)
    : MontaguTokenAuthenticator(signatureConfiguration, expectedIssuer)
{
    override fun createJwtProfile(credentials: TokenCredentials, jwt: JWT)
    {
        super.createJwtProfile(credentials, jwt)
        val claims = jwt.jwtClaimsSet
        val issuer = claims.issuer
        if (issuer != expectedIssuer)
        {
            throw CredentialsException("Token was issued by '$issuer'. Must be issued by '$expectedIssuer'")
        }
        credentials.userProfile.addAttribute("url", "*")
    }
}