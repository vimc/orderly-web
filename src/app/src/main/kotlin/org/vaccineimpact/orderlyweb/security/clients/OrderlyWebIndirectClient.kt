package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.client.DirectClient
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.credentials.extractor.CredentialsExtractor
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.core.redirect.RedirectAction
import org.pac4j.core.redirect.RedirectActionBuilder
import org.pac4j.core.util.CommonHelper
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator
import org.vaccineimpact.orderlyweb.security.authorization.orderlyWebPermissions
import javax.sql.CommonDataSource


class OrderlyWebIndirectClient() : IndirectClient<Credentials, CommonProfile>(){

    init {
        setCallbackUrl("/login")
    }

    override fun clientInit()
    {
        defaultCredentialsExtractor(NeverInvokedCredentialsExtractor())

        defaultRedirectActionBuilder(OrderlyWebIndirectClientRedirectActionBuilder())

        defaultAuthenticator(NeverInvokedAuthenticator())
    }

}

class OrderlyWebIndirectClientRedirectActionBuilder: RedirectActionBuilder
{
    override fun redirect(context: WebContext) : RedirectAction
    {
        val profile = CommonProfile().apply { id = "anon" }

        val permissions = OrderlyAuthorizationRepository()
                .getPermissionsForUser("anon")

        profile.orderlyWebPermissions = permissions.plus(ReifiedPermission("reports.read",
                Scope.Specific("report", "minimal")))

        val manager = ProfileManager<CommonProfile>(context)
        manager.save(true, profile, false)
        return RedirectAction.redirect(context.fullRequestURL)
    }
}

//The OrderlyIndirectClient is only used to forward the user on to the appropriate auth provider via the landing page.
//We need to provide a CredentialsExtractor object to keep pac4j happy, but it should never actually be invoked.
// Similarly for NeverInvokedAuthenticator
class NeverInvokedCredentialsExtractor: CredentialsExtractor<Credentials> {

    override fun extract(context: WebContext): Credentials
    {
        throw UnsupportedOperationException("NeverInvokedCredentialsExtractor should not be invoked.")
    }
}

class NeverInvokedAuthenticator: Authenticator<Credentials> {
    override fun validate(credentials: Credentials, context: WebContext) {
        throw UnsupportedOperationException("NeverInvokedAuthenticator should not be invoked.")
    }
}

