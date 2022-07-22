package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebAuthenticationConfig
import org.vaccineimpact.orderlyweb.viewmodels.AdminViewModel

class AdminController(context: ActionContext,
                      private val authConfig: AuthenticationConfig) : Controller(context)
{
    constructor(context: ActionContext) : this(context,
            OrderlyWebAuthenticationConfig())

    @Template("admin.ftl")
    fun admin(): AdminViewModel
    {
        return AdminViewModel(context, authConfig.canAllowGuestUser)
    }

}
