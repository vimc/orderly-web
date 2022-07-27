package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.ReportRunController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.secure
import org.vaccineimpact.orderlyweb.transform

object WebLogsRouteConfig : RouteConfig
{
    private val runReports = setOf("*/reports.run")

    override val endpoints = listOf(
            WebEndpoint(
                    "/reports/running/",
                    ReportRunController::class,
                    "runningReports"
            )
                    .json()
                    .secure(runReports)
                    .transform()
    )
}
