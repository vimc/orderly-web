package org.vaccineimpact.orderlyweb.security.authentication

import com.nimbusds.jwt.JWT
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.pac4j.jwt.config.signature.SignatureConfiguration
import java.text.ParseException

open class OrderlyWebBearerTokenAuthenticator(signatureConfiguration: SignatureConfiguration, expectedIssuer: String)
    : OrderlyWebTokenAuthenticator(signatureConfiguration, expectedIssuer)
{
    override fun createJwtProfile(credentials: TokenCredentials, jwt: JWT)
    {
        try
        {
            super.createJwtProfile(credentials, jwt)
        }
        catch (e: ParseException)
        {
            throw CredentialsException("Token is invalid.")
        }
        if (credentials.userProfile == null)
        {
            throw CredentialsException("Token has expired. Please request a new one.")
        }
        val claims = jwt.jwtClaimsSet
        val issuer = claims.issuer
        if (issuer != expectedIssuer)
        {
            throw CredentialsException("Token was issued by '$issuer'. Must be issued by '$expectedIssuer'")
        }
        credentials.userProfile.addAttribute("url", "*")
    }
}