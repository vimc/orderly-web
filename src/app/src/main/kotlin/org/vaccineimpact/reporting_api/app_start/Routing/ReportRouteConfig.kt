package org.vaccineimpact.reporting_api.app_start.Routing

import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.app_start.RouteConfig
import spark.route.HttpMethod

object ReportRouteConfig : RouteConfig
{
    private val readReports = setOf("*/reports.read")
    private val runReports = setOf("*/reports.run")

    override val endpoints: List<EndpointDefinition> = listOf(

            Endpoint("/reports/", "Report", "getAllNames")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/", "Report", "getVersionsByName")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/run/", "Report", "run",
                    method = HttpMethod.post)
                    .json()
                    .secure(runReports),

            Endpoint("/reports/:key/status/", "Report", "status")
                    .json()
                    .secure(runReports))
}