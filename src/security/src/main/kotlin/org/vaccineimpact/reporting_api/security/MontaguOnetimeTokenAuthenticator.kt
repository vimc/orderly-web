package org.vaccineimpact.reporting_api.security

import com.nimbusds.jwt.JWT
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.pac4j.jwt.config.signature.SignatureConfiguration

class MontaguOnetimeTokenAuthenticator(signatureConfiguration: SignatureConfiguration,
                                       expectedIssuer: String,
                                       private val tokenStore: OnetimeTokenStore)
    : MontaguTokenAuthenticator(signatureConfiguration, expectedIssuer)
{
    override fun createJwtProfile(credentials: TokenCredentials, jwt: JWT)
    {
        if (!tokenStore.validateOneTimeToken(credentials.token))
        {
            throw CredentialsException("Token has already been used (or never existed)")
        }

        super.createJwtProfile(credentials, jwt)

        val claims = jwt.jwtClaimsSet
        val sub = claims.subject
        val url = claims.getClaim("url")

        if (sub != TokenIssuer.oneTimeActionSubject)
        {
            throw CredentialsException("Expected 'sub' claim to be ${TokenIssuer.oneTimeActionSubject}")
        }
//        if (url.toString().isNullOrEmpty())
//        {
//                 throw CredentialsException("No 'url' claim provided. Token is invalid")
//        }
        credentials.userProfile.addAttribute(NEEDS_URL, true)

    }

}
