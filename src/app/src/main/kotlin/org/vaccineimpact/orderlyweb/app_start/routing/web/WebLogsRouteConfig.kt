package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.ReportRunController

object WebLogsRouteConfig : RouteConfig
{
    private val runReports = setOf("*/reports.run")

    override val endpoints = listOf(
            WebEndpoint("/reports/running/",
                    ReportRunController::class, "runningReports")
                    .json()
                    .secure(runReports)
                    .transform()
    )
}
