package org.vaccineimpact.orderlyweb.app_start.Routing

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.ReportController
import spark.route.HttpMethod

object ReportRouteConfig : RouteConfig
{
    private val runReports = setOf("*/reports.run")
    private val readReports = setOf("report:<name>/reports.read")
    private val reviewReports = setOf("*/reports.review")
    private val controller = ReportController::class

    override val endpoints: List<EndpointDefinition> = listOf(

            APIEndpoint("/reports/", controller, "getAllReports")
                    .json()
                    .transform()
                    // more specific permission checking in the controller action
                    .secure(),

            APIEndpoint("/reports/:name/", controller, "getVersionsByName")
                    .json()
                    .transform()
                    .secure(readReports),

            APIEndpoint("/reports/:name/run/", controller, "run",
                    method = HttpMethod.post)
                    .json()
                    .secure(runReports),

            APIEndpoint("/reports/:key/status/", controller, "status")
                    .json()
                    .secure(runReports),
            APIEndpoint("/reports/:name/latest/changelog/", controller, "getLatestChangelogByName")
                    .json()
                    .transform()
                    .secure(readReports)
    )
}