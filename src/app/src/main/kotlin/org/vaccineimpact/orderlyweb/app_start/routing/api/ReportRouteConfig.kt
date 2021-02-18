package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.ReportController
import org.vaccineimpact.orderlyweb.controllers.api.ReportRunController
import spark.route.HttpMethod

object ReportRouteConfig : RouteConfig
{
    private val runReports = setOf("*/reports.run")
    private val readReports = setOf("report:<name>/reports.read")

    override val endpoints: List<EndpointDefinition> = listOf(

            APIEndpoint("/reports/", ReportController::class, "getAllReports")
                    .json()
                    .transform()
                    // more specific permission checking in the controller action
                    .secure(),

            APIEndpoint("/reports/:name/", ReportController::class, "getVersionsByName")
                    .json()
                    .transform()
                    .secure(readReports),

            APIEndpoint("/reports/:name/run/", ReportRunController::class, "run",
                    method = HttpMethod.post)
                    .json()
                    .secure(runReports),

            APIEndpoint("/reports/:key/status/", ReportRunController::class, "status")
                    .json()
                    .secure(runReports),
            APIEndpoint("/reports/:key/kill/", ReportRunController::class, "kill",
                    method = HttpMethod.delete)
                    .json()
                    .secure(runReports),

            APIEndpoint("/reports/:name/latest/changelog/", ReportController::class, "getLatestChangelogByName")
                    .json()
                    .transform()
                    .secure(readReports)
    )
}
