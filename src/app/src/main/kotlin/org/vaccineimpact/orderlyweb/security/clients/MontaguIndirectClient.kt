package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.client.IndirectClient
import org.pac4j.core.context.WebContext
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.http.RedirectionAction
import org.pac4j.core.logout.LogoutActionBuilder
import org.pac4j.core.profile.UserProfile
import org.pac4j.core.redirect.RedirectionActionBuilder
import org.pac4j.core.util.HttpActionHelper
import org.pac4j.http.credentials.extractor.CookieExtractor
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.security.authentication.MontaguAuthenticator
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator
import org.vaccineimpact.orderlyweb.security.providers.MontaguAPIClient
import org.vaccineimpact.orderlyweb.security.providers.OkHttpMontaguAPIClient
import java.net.URLEncoder
import java.util.*

class MontaguIndirectClient : IndirectClient(), OrderlyWebTokenCredentialClient
{

    init
    {
        setCallbackUrl("/login")
    }

    override fun internalInit(forceReinit: Boolean)
    {
        val montaguAPIClient = OkHttpMontaguAPIClient.create()
        val cookieExtractor = CookieExtractor(cookie)
        defaultCredentialsExtractor(cookieExtractor)
        defaultRedirectionActionBuilder(MontaguIndirectClientRedirectActionBuilder(montaguAPIClient, cookieExtractor))

        defaultAuthenticator(MontaguAuthenticator(OrderlyUserRepository(), montaguAPIClient))

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

class MontaguIndirectClientRedirectActionBuilder(private val montaguAPIClient: MontaguAPIClient,
                                                 private val cookieExtractor: CookieExtractor,
                                                 private val appConfig: AppConfig = AppConfig()) : RedirectionActionBuilder
{
    override fun getRedirectionAction(context: WebContext, sessionStore: SessionStore): Optional<RedirectionAction>
    {
        val loginCallbackUrl = appConfig["app.url"] + "/login"

        val redirectUrl = try
        {
            val token = (cookieExtractor
                    .extract(context, sessionStore)
                    .get() as TokenCredentials)
                    .token

            montaguAPIClient.getUserDetails(token)

            // already logged in to Montagu, so send user straight to the login callback
            loginCallbackUrl
        }
        catch (e: Exception)
        {
            // not already logged in to Montagu so redirect to Montagu
            "${appConfig["montagu.url"]}?redirectTo=${URLEncoder.encode(loginCallbackUrl, "utf-8")}"
        }

        return Optional.of(HttpActionHelper.buildRedirectUrlAction(context, redirectUrl))
    }
}

class MontaguLogoutActionBuilder : LogoutActionBuilder
{
    override fun getLogoutAction(context: WebContext, sessionStore: SessionStore,
                                 currentProfile: UserProfile,
                                 targetUrl: String): Optional<RedirectionAction>
    {
        return Optional.of(HttpActionHelper.buildRedirectUrlAction(context, AppConfig()["app.url"]))
    }
}