package org.vaccineimpact.reporting_api.security

import com.nimbusds.jwt.JWT
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.pac4j.jwt.config.signature.SignatureConfiguration
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator

class MontaguOnetimeTokenAuthenticator(signatureConfiguration: SignatureConfiguration,
                                       val expectedIssuer: String,
                                       private val tokenStore: OnetimeTokenStore)
    : JwtAuthenticator(signatureConfiguration)
{
    override fun createJwtProfile(credentials: TokenCredentials, jwt: JWT)
    {
        if (!tokenStore.validateOneTimeToken(credentials.token))
        {
            throw CredentialsException("Token has already been used (or never existed)")
        }

        super.createJwtProfile(credentials, jwt)
        val claims = jwt.jwtClaimsSet
        val issuer = claims.issuer
        val sub = claims.subject
        if (issuer != expectedIssuer)
        {
            throw CredentialsException("Token was issued by '$issuer'. Must be issued by '$expectedIssuer'")
        }
        if (sub != TokenIssuer.oneTimeActionSubject)
        {
            throw CredentialsException("Expected 'sub' claim to be ${TokenIssuer.oneTimeActionSubject}")
        }
    }

}
