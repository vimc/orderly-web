package org.vaccineimpact.orderlyweb.security

import org.pac4j.http.client.direct.ParameterClient
import org.vaccineimpact.api.models.ErrorInfo

class JWTParameterClientWrapper(helper: TokenVerifier,
                                tokenStore: OnetimeTokenStore)
    : MontaguCredentialClientWrapper
{
    override val errorInfo = ErrorInfo("onetime-token-invalid", "Onetime token not supplied, or onetime token was invalid")
    override val client = JWTParameterClient(helper, tokenStore)
}

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTParameterClient(helper: TokenVerifier, tokenStore: OnetimeTokenStore) : ParameterClient(
        "access_token",
        MontaguOnetimeTokenAuthenticator(helper.signatureConfiguration,
                helper.expectedIssuer,
                tokenStore))
{
    init
    {
        this.isSupportGetRequest = true
    }
}