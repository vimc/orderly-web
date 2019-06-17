package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.secure
import spark.route.HttpMethod

object WebReportRouteConfig : RouteConfig
{
    private val readReports = setOf("report:<name>/reports.read")
    private val runReports = setOf("*/reports.run")
    override val endpoints = listOf(
            WebEndpoint("/report/:name/:version/",
                    ReportController::class, "getByNameAndVersion")
                    .secure(readReports),
            WebEndpoint("/report/:name/run/",
                    org.vaccineimpact.orderlyweb.controllers.api.ReportController::class, "run",
                    method = HttpMethod.post)
                    .json()
                    .secure(runReports),
            WebEndpoint("/report/:key/status/",
                    org.vaccineimpact.orderlyweb.controllers.api.ReportController::class, "status")
                    .json()
                    .secure(runReports)

    )
}