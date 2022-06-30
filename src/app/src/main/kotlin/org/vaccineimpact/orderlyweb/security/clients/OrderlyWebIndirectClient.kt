package org.vaccineimpact.orderlyweb.security.clients

import java.net.URLEncoder
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.context.WebContext
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.credentials.extractor.CredentialsExtractor
import org.pac4j.core.exception.http.RedirectionAction
import org.pac4j.core.redirect.RedirectionActionBuilder
import org.pac4j.core.util.HttpActionHelper
import org.vaccineimpact.orderlyweb.db.AppConfig

class OrderlyWebIndirectClient() : IndirectClient() {
    
    init {
        setCallbackUrl("/login")
    }

    override fun internalInit(forceReinit: Boolean)
    {
        defaultCredentialsExtractor(NeverInvokedCredentialsExtractor())
        defaultRedirectionActionBuilder(OrderlyWebIndirectClientRedirectActionBuilder())
        defaultAuthenticator(NeverInvokedAuthenticator())
    }

}

class OrderlyWebIndirectClientRedirectActionBuilder: RedirectionActionBuilder
{
    override fun getRedirectionAction(context: WebContext, sessionStore: SessionStore) : java.util.Optional<RedirectionAction>
    {
        //Attach the originally requested url to the redirect url as a query string parameter, so we can redirect
        //there once authenticated via the 'landing page'
        val requestedUrl = context.fullRequestURL
        val encodedUrl = URLEncoder.encode(requestedUrl, "utf-8")
        val url = AppConfig()["app.url"] + "/weblogin?requestedUrl=" + encodedUrl
        return java.util.Optional.of<RedirectionAction?>(HttpActionHelper.buildRedirectUrlAction(context, url))
    }
}

//The OrderlyIndirectClient is only used to forward the user on to the appropriate auth provider via the landing page.
//We need to provide a CredentialsExtractor object to keep pac4j happy, but it should never actually be invoked.
// Similarly for NeverInvokedAuthenticator
class NeverInvokedCredentialsExtractor: CredentialsExtractor {

    override fun extract(context: WebContext, sessionStore: SessionStore): java.util.Optional<Credentials>
    {
        throw UnsupportedOperationException("NeverInvokedCredentialsExtractor should not be invoked.")
    }
}

class NeverInvokedAuthenticator: Authenticator {

    override fun validate(credentials: Credentials, context: WebContext, sessionStore: SessionStore) {
        throw UnsupportedOperationException("NeverInvokedAuthenticator should not be invoked.")
    }
}

