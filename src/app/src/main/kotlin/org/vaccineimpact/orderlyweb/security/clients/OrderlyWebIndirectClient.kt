package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.client.IndirectClient
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.credentials.extractor.CredentialsExtractor
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.redirect.RedirectAction
import org.pac4j.core.redirect.RedirectActionBuilder
import org.vaccineimpact.orderlyweb.db.AppConfig
import java.net.URLEncoder

class OrderlyWebIndirectClient() : IndirectClient<Credentials, CommonProfile>()
{

    init
    {
        setCallbackUrl("/login")
    }

    override fun clientInit()
    {
        defaultCredentialsExtractor(NeverInvokedCredentialsExtractor())

        defaultRedirectActionBuilder(OrderlyWebIndirectClientRedirectActionBuilder())

        defaultAuthenticator(NeverInvokedAuthenticator())
    }
}

class OrderlyWebIndirectClientRedirectActionBuilder : RedirectActionBuilder
{
    override fun redirect(context: WebContext): RedirectAction
    {
        //Attach the originally requested url to the redirect url as a query string parameter, so we can redirect
        //there once authenticated via the 'landing page'
        val requestedUrl = context.fullRequestURL
        val encodedUrl = URLEncoder.encode(requestedUrl, "utf-8")
        return RedirectAction.redirect(AppConfig()["app.url"] + "/weblogin?requestedUrl=" + encodedUrl)
    }
}

//The OrderlyIndirectClient is only used to forward the user on to the appropriate auth provider via the landing page.
//We need to provide a CredentialsExtractor object to keep pac4j happy, but it should never actually be invoked.
// Similarly for NeverInvokedAuthenticator
class NeverInvokedCredentialsExtractor : CredentialsExtractor<Credentials>
{

    override fun extract(context: WebContext): Credentials
    {
        throw UnsupportedOperationException("NeverInvokedCredentialsExtractor should not be invoked.")
    }
}

class NeverInvokedAuthenticator : Authenticator<Credentials>
{
    override fun validate(credentials: Credentials, context: WebContext)
    {
        throw UnsupportedOperationException("NeverInvokedAuthenticator should not be invoked.")
    }
}
