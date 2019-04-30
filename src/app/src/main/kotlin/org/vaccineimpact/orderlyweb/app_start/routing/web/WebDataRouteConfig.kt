package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.DataController

object WebDataRouteConfig : RouteConfig
{
    private val readReports = setOf("*/reports.read")
    private val controller = DataController::class

    override val endpoints: List<EndpointDefinition> = listOf(
            WebEndpoint("/data/csv/:id/", controller, "downloadCSV")
                    .secure(readReports),

            WebEndpoint("/data/rds/:id/", controller, "downloadRDS")
                    .secure(readReports)
    )
}