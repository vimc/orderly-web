package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.app_start.Router
import org.vaccineimpact.reporting_api.db.Config

class HomeController(context: ActionContext)
    : Controller(context)
{
    fun index() = Index("montagu-reports", Config["app.version"], Router.urls)

    data class Index(val name: String, val version: String, val endpoints: List<String>)
}
