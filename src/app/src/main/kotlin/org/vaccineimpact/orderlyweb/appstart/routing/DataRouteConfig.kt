package org.vaccineimpact.orderlyweb.appstart.routing

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.appstart.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.DataController

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