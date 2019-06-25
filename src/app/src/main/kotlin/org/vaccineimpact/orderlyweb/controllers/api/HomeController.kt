package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.app_start.OrderlyWeb
import org.vaccineimpact.orderlyweb.app_start.Router
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config

class HomeController(context: ActionContext, private val config: Config)
    : Controller(context)
{
    constructor(context: ActionContext) :
            this(context, AppConfig())

    fun index() = Index(this.config["app.name"], this.config["app.version"], OrderlyWeb.urls)

    data class Index(val name: String, val version: String, val endpoints: List<String>)
}
