package org.vaccineimpact.reporting_api.security

import com.nimbusds.jwt.JWT
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator

class MontaguTokenAuthenticator(private val tokenHelper: WebTokenHelper)
    : JwtAuthenticator(tokenHelper.signatureConfiguration)
{
    override fun createJwtProfile(credentials: TokenCredentials, jwt: JWT)
    {
        super.createJwtProfile(credentials, jwt)
        val claims = jwt.jwtClaimsSet
        val issuer = claims.issuer
        if (issuer != tokenHelper.issuer)
        {
            throw CredentialsException("Token was issued by '$issuer'. Must be issued by '${tokenHelper.issuer}'")
        }
    }
}