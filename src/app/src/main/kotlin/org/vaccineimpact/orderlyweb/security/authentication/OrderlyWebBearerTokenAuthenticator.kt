package org.vaccineimpact.orderlyweb.security.authentication

import com.nimbusds.jwt.JWT
import org.pac4j.core.context.WebContext
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.pac4j.jwt.config.signature.SignatureConfiguration
import org.vaccineimpact.orderlyweb.errors.ExpiredToken

open class OrderlyWebBearerTokenAuthenticator(
        signatureConfiguration: SignatureConfiguration, expectedIssuer: String
) : OrderlyWebTokenAuthenticator(signatureConfiguration, expectedIssuer)
{
    override fun createJwtProfile(
            credentials: TokenCredentials,
            jwt: JWT, context: WebContext,
            sessionStore: SessionStore
    )
    {
        super.createJwtProfile(credentials, jwt, context, sessionStore)

        if (credentials.userProfile == null)
        {
            throw ExpiredToken()
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
