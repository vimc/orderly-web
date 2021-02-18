package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
// import org.vaccineimpact.orderlyweb.controllers.api.ReportRunController
// import org.vaccineimpact.orderlyweb.controllers.web.IndexController
import org.vaccineimpact.orderlyweb.controllers.web.LogsController
// import spark.route.HttpMethod

object WebLogsRouteConfig : RouteConfig
{
    // private val readReports = setOf("report:<name>/reports.read")
    private val runReports = setOf("*/reports.run")
    // private val reviewReports = setOf("*/reports.review")
    // private val configureReports = setOf("*/pinned-reports.manage")

    override val endpoints = listOf(
            WebEndpoint("/running/",
                    LogsController::class, "running")
                    .json()
                    .secure(runReports)
                    .transform()
    )
}
