package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.client.IndirectClient
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.redirect.RedirectAction
import org.pac4j.http.credentials.extractor.CookieExtractor
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.security.authentication.MontaguAuthenticator
import org.vaccineimpact.orderlyweb.security.providers.khttpMontaguAPIClient

class MontaguIndirectClient : IndirectClient<TokenCredentials, CommonProfile>(), OrderlyWebTokenCredentialClient {

    init {
        setCallbackUrl("/login")
    }

    override fun clientInit()
    {
        defaultCredentialsExtractor(CookieExtractor("montagu_jwt_token"))
        defaultRedirectActionBuilder {
            RedirectAction.redirect(AppConfig()["montagu.url"])
        }
        defaultAuthenticator(MontaguAuthenticator(OrderlyUserRepository(), khttpMontaguAPIClient()))
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