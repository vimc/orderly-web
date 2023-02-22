package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.ReportRunController
import spark.route.HttpMethod

object RunReportRouteConfig : RouteConfig
{
    private val runReports = setOf("*/reports.run")

    override val endpoints: List<EndpointDefinition> = listOf(

            APIEndpoint(
                    "/reports/:name/run/",
                    ReportRunController::class,
                    "run",
                    method = HttpMethod.post
            )
            .json()
            .secure(runReports),

            APIEndpoint(
                    "/reports/:key/status/",
                    ReportRunController::class,
                    "status"
            )
            .json()
            .secure(runReports),

            APIEndpoint(
                    "/reports/:key/kill/",
                    ReportRunController::class,
                    "kill",
                    method = HttpMethod.delete
            )
            .json()
            .secure(runReports)
    )
}
