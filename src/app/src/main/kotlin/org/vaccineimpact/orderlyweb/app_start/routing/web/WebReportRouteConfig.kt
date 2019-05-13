package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.secure

object WebReportRouteConfig : RouteConfig
{
    private val readReports = setOf("report:<name>/reports.read")
    override val endpoints = listOf(
            WebEndpoint("/reports/:name/:version/",
                    ReportController::class, "getByNameAndVersion")
                    .secure(readReports)
    )
}