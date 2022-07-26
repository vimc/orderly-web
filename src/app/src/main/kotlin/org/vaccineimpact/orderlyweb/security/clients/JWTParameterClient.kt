package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.http.client.direct.ParameterClient
import org.vaccineimpact.orderlyweb.db.OnetimeTokenStore
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebOnetimeTokenAuthenticator
import org.vaccineimpact.orderlyweb.security.authentication.RSATokenVerifier
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTParameterClient(helper: RSATokenVerifier, tokenStore: OnetimeTokenStore) :
        OrderlyWebTokenCredentialClient, ParameterClient(
        "access_token",
        OrderlyWebOnetimeTokenAuthenticator(
                helper.signatureConfiguration,
                helper.expectedIssuer,
                tokenStore
        )
)
{
    init
    {
        this.isSupportGetRequest = true
    }

    override fun internalInit(forceReinit: Boolean)
    {
        setAuthorizationGenerator(OrderlyAuthorizationGenerator())
        super.internalInit(forceReinit)
    }

    override val errorInfo = ErrorInfo(
            "onetime-token-invalid",
            "Onetime token not supplied, or onetime token was invalid"
    )
}
