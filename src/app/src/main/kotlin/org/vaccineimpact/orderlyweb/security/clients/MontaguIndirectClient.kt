package org.vaccineimpact.orderlyweb.security.clients

import java.net.URLEncoder
import org.pac4j.core.client.IndirectClient

import org.pac4j.core.context.Cookie
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.logout.LogoutActionBuilder
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.redirect.RedirectAction
import org.pac4j.core.redirect.RedirectActionBuilder
import org.pac4j.http.credentials.extractor.CookieExtractor
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.security.authentication.MontaguAuthenticator
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator
import org.vaccineimpact.orderlyweb.security.providers.okhttpMontaguAPIClient

class MontaguIndirectClient : IndirectClient<TokenCredentials, CommonProfile>(), OrderlyWebTokenCredentialClient
{

    init {
        setCallbackUrl("/login")
    }

    override fun clientInit()
    {
        defaultCredentialsExtractor(CookieExtractor(cookie))
        defaultRedirectActionBuilder(MontaguIndirectClientRedirectActionBuilder())

        defaultAuthenticator(MontaguAuthenticator(OrderlyUserRepository(), okhttpMontaguAPIClient()))
        setAuthorizationGenerator(OrderlyAuthorizationGenerator())

        defaultLogoutActionBuilder(MontaguLogoutActionBuilder())
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

class MontaguIndirectClientRedirectActionBuilder: RedirectActionBuilder {
    override fun redirect(context: WebContext?): RedirectAction {
        val loginUrl = URLEncoder.encode(AppConfig()["app.url"] + "/login", "utf-8")
        val montaguUrl = AppConfig()["montagu.url"]
        val redirectUrl = "$montaguUrl?redirectTo=$loginUrl";
        return RedirectAction.redirect(redirectUrl)
    }
}

class MontaguLogoutActionBuilder : LogoutActionBuilder<CommonProfile>
{
    override fun getLogoutAction(context: WebContext, currentProfile: CommonProfile, targetUrl: String?): RedirectAction
    {
        //logout of Montagu by resetting token cookies
        listOf("montagu_jwt_token", "jwt_token").forEach{
            val cookie = Cookie(it, "")
            cookie.domain = context.serverName
            context.addResponseCookie(cookie)
        }

        return RedirectAction.redirect(AppConfig()["app.url"])
    }
}