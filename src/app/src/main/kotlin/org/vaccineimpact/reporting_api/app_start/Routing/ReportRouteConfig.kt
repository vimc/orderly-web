package org.vaccineimpact.reporting_api.app_start.Routing

import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.app_start.RouteConfig
import spark.route.HttpMethod

object ReportRouteConfig : RouteConfig
{
    override val endpoints: List<EndpointDefinition> = listOf(

            Endpoint("/reports/", "Report", "getAllNames")
                    .json()
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/", "Report", "getVersionsByName")
                    .json()
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/run/", "Report", "run",
                    method = HttpMethod.post)
                    .json()
                    .secure(setOf("*/reports.run")),

            Endpoint("/reports/:key/status/", "Report", "status")
                    .json()
                    .secure(setOf("*/reports.run")))
}