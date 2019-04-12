package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.controllers.api.Template
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.viewmodels.AppViewModel

class SecurityController(actionContext: ActionContext,
                     private val orderly: OrderlyClient) : Controller(actionContext)
{
    constructor(actionContext: ActionContext) : this(actionContext, Orderly())

    class WebloginViewModel(context: ActionContext) : AppViewModel(context) //TODO: include supported auth method here

    @Template("weblogin.ftl")
    fun weblogin(): WebloginViewModel
    {
        return WebloginViewModel(context)
    }

}