package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.http.client.direct.ParameterClient
import org.vaccineimpact.orderlyweb.db.OnetimeTokenStore
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebOnetimeTokenAuthenticator
import org.vaccineimpact.orderlyweb.security.authentication.TokenVerifier

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

    override fun clientInit()
    {
        setAuthorizationGenerator(OrderlyAuthorizationRepository())
        super.clientInit()
    }

    override val errorInfo = ErrorInfo("onetime-token-invalid", "Onetime token not supplied, or onetime token was invalid")
}