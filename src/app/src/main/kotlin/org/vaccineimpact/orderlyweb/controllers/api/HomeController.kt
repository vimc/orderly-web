package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.appstart.Router
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config

class HomeController(context: ActionContext, private val config: Config)
    : Controller(context)
{
    constructor(context: ActionContext) :
            this(context, AppConfig())

    fun index() = Index("montagu-reports", this.config["app.version"], Router.urls)

    data class Index(val name: String, val version: String, val endpoints: List<String>)
}
