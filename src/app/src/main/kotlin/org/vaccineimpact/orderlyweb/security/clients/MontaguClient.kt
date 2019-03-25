package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.context.WebContext
import org.pac4j.http.client.direct.CookieClient
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.security.authentication.MontaguAuthenticator
import org.vaccineimpact.orderlyweb.security.authorization.AuthorizationRepository

class MontaguClient : OrderlyWebTokenCredentialClient, CookieClient(
        cookie,
        MontaguAuthenticator()
)
{
    override fun clientInit(context: WebContext?)
    {
        setAuthorizationGenerator(AuthorizationRepository())
        super.clientInit(context)
    }

    companion object
    {
        const val cookie = "montagu_jwt_token"
    }

    override val errorInfo = ErrorInfo(
            "montagu-cookie-bearer-token-invalid",
            "Montagu bearer token not supplied in cookie '$cookie', or bearer token was invalid"
    )
}
