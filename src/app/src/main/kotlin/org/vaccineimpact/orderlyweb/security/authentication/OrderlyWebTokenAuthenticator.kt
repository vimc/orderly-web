package org.vaccineimpact.orderlyweb.security.authentication

import org.pac4j.jwt.config.signature.SignatureConfiguration
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator

abstract class OrderlyWebTokenAuthenticator(signatureConfiguration: SignatureConfiguration,
                                            protected val expectedIssuer: String)
    : JwtAuthenticator(signatureConfiguration)
