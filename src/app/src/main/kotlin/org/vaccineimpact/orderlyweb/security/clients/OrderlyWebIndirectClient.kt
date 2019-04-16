package org.vaccineimpact.orderlyweb.security.clients

import java.net.URLEncoder
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.credentials.extractor.CredentialsExtractor
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.redirect.RedirectAction
import org.vaccineimpact.orderlyweb.db.AppConfig


class OrderlyWebIndirectClient() : IndirectClient<TokenCredentials, CommonProfile>(){

    init {
        setCallbackUrl("/login")
    }

    override fun clientInit()
    {
        defaultCredentialsExtractor(OrderlyWebCredentialsExtractor())

        //TODO: When implementing https://vimc.myjetbrains.com/youtrack/issue/mrc-228 we might put logic to choose
        //to redirect to weblogin or to configured Montagu homepage (skip our landing page for Montagu users)
        defaultRedirectActionBuilder {
            //Attach the originally requested url to the redirect url as a query string parameter, so we can redirect
            //there once authenticated via the 'landing page'
            val requestedUrl = it.fullRequestURL
            val encodedUrl = URLEncoder.encode(requestedUrl, "utf-8")
            RedirectAction.redirect(AppConfig()["app.url"] + "/weblogin?requestedUrl=" + encodedUrl)
        }

        defaultAuthenticator(OrderlyWebAuthenticator())
    }


}

//The OrderlyIndirectClient is only used to forward the user on to the appropriate auth provider via the landing page.
//We need to provide a CredentialsExtractor object to keep pac4j happy, but it should never actually be invoked, so this
//is just a dummy class. Similarly for OrderlyWebAuthenticator
class OrderlyWebCredentialsExtractor: CredentialsExtractor<TokenCredentials> {

    override fun extract(context: WebContext) :TokenCredentials
    {
        throw UnsupportedOperationException("OrderlyWebCredentialsExtractor should not be invoked.")
    }
}

class OrderlyWebAuthenticator: Authenticator<TokenCredentials> {
    override fun validate(credentials: TokenCredentials, context: WebContext) {
        throw UnsupportedOperationException("OrderlyWebAuthenticator should not be invoked.")
    }
}

