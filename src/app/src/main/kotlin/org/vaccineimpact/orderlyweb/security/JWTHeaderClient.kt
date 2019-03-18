package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.context.WebContext
import org.pac4j.http.client.direct.HeaderClient
import org.vaccineimpact.orderlyweb.models.ErrorInfo

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTHeaderClient(helper: TokenVerifier) : OrderlyWebTokenCredentialClient, HeaderClient(
        "Authorization",
        "Bearer ",
        OrderlyWebBearerTokenAuthenticator(helper.signatureConfiguration, helper.expectedIssuer))

{
    override val errorInfo = ErrorInfo("bearer-token-invalid",
            "Bearer token not supplied in Authorization header, or bearer token was invalid")

    override fun clientInit(context: WebContext?)
    {
        addAuthorizationGenerator{ _, profile -> extractPermissionsFromToken(profile) }
        super.clientInit(context)
    }
}