package org.vaccineimpact.reporting_api.app_start.Routing

import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.app_start.RouteConfig
import org.vaccineimpact.reporting_api.controllers.DataController

object DataRouteConfig : RouteConfig {
    private val readReports = setOf("*/reports.read")
    private val controller = DataController::class

    override val endpoints: List<EndpointDefinition> = listOf(
            Endpoint("/data/csv/:id/", controller, "downloadCSV", ContentTypes.csv)
                    .secure(readReports)
                    .allowParameterAuthentication(),

            Endpoint("/data/rds/:id/", controller, "downloadRDS")
                    .secure(readReports)
                    .allowParameterAuthentication()
    )
}