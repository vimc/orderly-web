package org.vaccineimpact.reporting_api.app_start.Routing

import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.app_start.RouteConfig

object DataRouteConfig : RouteConfig
{
    private val readReports = setOf("*/reports.read")

    override val endpoints: List<EndpointDefinition> = listOf(
            Endpoint("/data/csv/:id/", "Data", "downloadCSV", ContentTypes.csv)
                    .secure(readReports)
                    .allowParameterAuthentication(),

            Endpoint("/data/rds/:id/", "Data", "downloadRDS")
                    .secure(readReports)
                    .allowParameterAuthentication()
    )
}