package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.client.IndirectClient
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.credentials.extractor.CredentialsExtractor
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.redirect.RedirectAction
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.security.authentication.MontaguAuthenticator
import org.vaccineimpact.orderlyweb.security.providers.khttpMontaguAPIClient


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
            RedirectAction.redirect(AppConfig()["app.url"] + "/weblogin")
        }

        defaultAuthenticator(OrderlyWebAuthenticator())
    }


}

//The OrderlyIndirectClient is only used to forward the user on to the appropriate auth provider via the landing page.
//We need to provide a CredentialsExtractor object to keep pac4j happy, but it should never actually be invoked, so this
//is really just a dummy class. Similarly for OrderlyWebAuthenticator
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

