package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.controllers.api.Template
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.viewmodels.AppViewModel

class SecurityController(actionContext: ActionContext,
                     private val orderly: OrderlyClient) : Controller(actionContext)
{
    constructor(actionContext: ActionContext) : this(actionContext, Orderly())

    class WebloginViewModel(context: ActionContext, val authProvider: String, val requestedUrl: String) : AppViewModel(context)

    @Template("weblogin.ftl")
    fun weblogin(): WebloginViewModel
    {
        //This action handles displaying the 'landing page' with links to the external auth providers e.g. Github
        //This is the redirect location for the OrderlyWebIndirectClient, which secures the WebEndpoints of the app

        val authProvider = AuthenticationConfig.getConfiguredProvider().toString()
        val requestedUrl = context.queryParams("requestedUrl")
        return WebloginViewModel(context, authProvider, requestedUrl?:"")
    }

    fun webloginExternal()
    {
        //This action handles the redirect back from the external auth provider after successful authentication (this
        //endpoint is secured by the client for the configured auth provider). We redirect to the user's originally
        //requested route - the security filter for that route will check whether the authenticated user has sufficient
        //permissions
        val requestedUrl = context.queryParams("requestedUrl")
        context.getSparkResponse().redirect(requestedUrl)
    }

}