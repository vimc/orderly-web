package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.viewmodels.AdminViewModel

class AdminController(context: ActionContext) : Controller(context)
{
    @Template("admin.ftl")
    fun admin(): AdminViewModel
    {
        return AdminViewModel(context)
    }

}