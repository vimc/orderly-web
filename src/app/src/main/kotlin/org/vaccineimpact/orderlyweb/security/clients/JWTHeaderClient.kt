package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.http.client.direct.HeaderClient
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebBearerTokenAuthenticator
import org.vaccineimpact.orderlyweb.security.authentication.TokenVerifier
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTHeaderClient(helper: TokenVerifier) : OrderlyWebTokenCredentialClient, HeaderClient(
        "Authorization",
        "Bearer ",
        OrderlyWebBearerTokenAuthenticator(helper.signatureConfiguration, helper.expectedIssuer))

{
    override val errorInfo = ErrorInfo("bearer-token-invalid",
            "Bearer token not supplied in Authorization header, or bearer token was invalid")

    override fun clientInit()
    {
        setAuthorizationGenerator(OrderlyAuthorizationGenerator())
        super.clientInit()
    }
}