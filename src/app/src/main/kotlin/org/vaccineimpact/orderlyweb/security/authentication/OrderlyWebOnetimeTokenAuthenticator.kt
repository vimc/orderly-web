package org.vaccineimpact.orderlyweb.security.authentication

import com.nimbusds.jwt.JWT
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.pac4j.jwt.config.signature.SignatureConfiguration
import org.vaccineimpact.orderlyweb.db.OnetimeTokenStore
import org.vaccineimpact.orderlyweb.security.issuing.TokenIssuer

class OrderlyWebOnetimeTokenAuthenticator(
        signatureConfiguration: SignatureConfiguration,
        expectedIssuer: String,
        private val tokenStore: OnetimeTokenStore
) : OrderlyWebTokenAuthenticator(signatureConfiguration, expectedIssuer)
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

        if (!tokenStore.validateOneTimeToken(credentials.token))
        {
            throw CredentialsException("Token has already been used (or never existed)")
        }

        val sub = claims.subject
        if (sub != TokenIssuer.oneTimeActionSubject)
        {
            throw CredentialsException("Expected 'sub' claim to be ${TokenIssuer.oneTimeActionSubject}")
        }

        val url = claims.getClaim("url")
        if (url !is String || url.isEmpty())
        {
            throw CredentialsException("No 'url' claim provided. Token is invalid")
        }

        credentials.userProfile?.setId(claims.getClaim("id"))
    }

}
