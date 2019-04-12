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

    class WebloginViewModel(context: ActionContext, val authProvider: String) : AppViewModel(context)

    @Template("weblogin.ftl")
    fun weblogin(): WebloginViewModel
    {
        val authProvider = AuthenticationConfig.getConfiguredProvider().toString()
        return WebloginViewModel(context, authProvider)
    }

}