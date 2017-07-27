package org.vaccineimpact.reporting_api.security

import com.nimbusds.jose.JOSEException
import com.nimbusds.jwt.*
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.exception.HttpAction
import org.pac4j.jwt.config.signature.SignatureConfiguration
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import java.text.ParseException

class MontaguOnetimeTokenAuthenticator(signatureConfiguration: SignatureConfiguration,
                                       val expectedIssuer: String,
                                       private val tokenStore: OnetimeTokenStore)
    : JwtAuthenticator(signatureConfiguration)
{
    fun createJwtProfile(credentials: TokenCredentials, jwt: JWT, context: WebContext)
    {
        if (!tokenStore.validateOneTimeToken(credentials.token))
        {
            throw CredentialsException("Token has already been used (or never existed)")
        }

        super.createJwtProfile(credentials, jwt)

        val claims = jwt.jwtClaimsSet
        val issuer = claims.issuer
        val sub = claims.subject
        val url = claims.getClaim("url")

        if (issuer != expectedIssuer)
        {
            throw CredentialsException("Token was issued by '$issuer'. Must be issued by '$expectedIssuer'")
        }
        if (sub != TokenIssuer.oneTimeActionSubject)
        {
            throw CredentialsException("Expected 'sub' claim to be ${TokenIssuer.oneTimeActionSubject}")
        }
        if (url != context.path)
        {
            throw CredentialsException("Expected 'url' claim to be of ${context.path}.")
        }

    }

    // This function is almost identical to the base class, except that we call our custom
    // function to create the JwtProfile using the current WebContext as well as the credentials
    @Throws(HttpAction::class, CredentialsException::class)
    override fun validate(credentials: TokenCredentials, context: WebContext)
    {
        init(context)
        val token = credentials.token

        try
        {
            // Parse the token
            var jwt = JWTParser.parse(token)

            if (jwt is PlainJWT)
            {
                if (signatureConfigurations.isEmpty())
                {
                    logger.debug("JWT is not signed and no signature configurations -> verified")
                }
                else
                {
                    throw CredentialsException("A non-signed JWT cannot be accepted as signature configurations have been defined")
                }
            }
            else
            {

                var signedJWT: SignedJWT? = null
                if (jwt is SignedJWT)
                {
                    signedJWT = jwt
                }

                // encrypted?
                if (jwt is EncryptedJWT)
                {
                    logger.debug("JWT is encrypted")

                    val encryptedJWT = jwt
                    var found = false
                    val header = encryptedJWT.header
                    val algorithm = header.algorithm
                    val method = header.encryptionMethod
                    for (config in encryptionConfigurations)
                    {
                        if (config.supports(algorithm, method))
                        {
                            logger.debug("Using encryption configuration: {}", config)
                            try
                            {
                                config.decrypt(encryptedJWT)
                                signedJWT = encryptedJWT.payload.toSignedJWT()
                                if (signedJWT != null)
                                {
                                    jwt = signedJWT
                                }
                                found = true
                                break
                            }
                            catch (e: JOSEException)
                            {
                                logger.debug("Decryption fails with encryption configuration: {}, passing to the next one", config)
                            }

                        }
                    }
                    if (!found)
                    {
                        throw CredentialsException("No encryption algorithm found for JWT: " + token)
                    }
                }

                // signed?
                if (signedJWT != null)
                {
                    logger.debug("JWT is signed")

                    var verified = false
                    var found = false
                    val algorithm = signedJWT.header.algorithm
                    for (config in signatureConfigurations)
                    {
                        if (config.supports(algorithm))
                        {
                            logger.debug("Using signature configuration: {}", config)
                            try
                            {
                                verified = config.verify(signedJWT)
                                found = true
                                if (verified)
                                {
                                    break
                                }
                            }
                            catch (e: JOSEException)
                            {
                                logger.debug("Verification fails with signature configuration: {}, passing to the next one", config)
                            }

                        }
                    }
                    if (!found)
                    {
                        throw CredentialsException("No signature algorithm found for JWT: " + token)
                    }
                    if (!verified)
                    {
                        throw CredentialsException("JWT verification failed: " + token)
                    }
                }
            }

            // this line differs from the base class
            createJwtProfile(credentials, jwt, context)

        }
        catch (e: ParseException)
        {
            throw CredentialsException("Cannot decrypt / verify JWT", e)
        }

    }


}
