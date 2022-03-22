package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.DataController

object DataRouteConfig : RouteConfig
{
    private val readReports = setOf("*/reports.read")
    private val controller = DataController::class

    override val endpoints: List<EndpointDefinition> = listOf(
            APIEndpoint("/data/csv/:id/", controller, "downloadCSV", ContentTypes.csv)
                    .secure(readReports)
                    .allowParameterAuthentication(),

            APIEndpoint("/data/rds/:id/", controller, "downloadRDS")
                    .secure(readReports)
                    .allowParameterAuthentication()
    )
}