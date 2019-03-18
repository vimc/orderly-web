package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.context.WebContext
import org.pac4j.http.client.direct.ParameterClient
import org.vaccineimpact.orderlyweb.models.ErrorInfo

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTParameterClient(helper: TokenVerifier, tokenStore: OnetimeTokenStore) : OrderlyWebTokenCredentialClient, ParameterClient(
        "access_token",
        OrderlyWebOnetimeTokenAuthenticator(helper.signatureConfiguration,
                helper.expectedIssuer,
                tokenStore))
{
    init
    {
        this.isSupportGetRequest = true
    }

    override fun clientInit(context: WebContext?)
    {
        addAuthorizationGenerator{ _, profile -> extractPermissionsFromToken(profile) }
        super.clientInit(context)
    }

    override val errorInfo = ErrorInfo("onetime-token-invalid", "Onetime token not supplied, or onetime token was invalid")
}