package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.app_start.Router
import org.vaccineimpact.reporting_api.db.AppConfig
import org.vaccineimpact.reporting_api.db.Config

class HomeController(context: ActionContext, private val config: Config)
    : Controller(context)
{
    constructor(context: ActionContext) :
            this(context, AppConfig())

    fun index() = Index("montagu-reports", this.config["app.version"], Router.urls)

    data class Index(val name: String, val version: String, val endpoints: List<String>)
}
