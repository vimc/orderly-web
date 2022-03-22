package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.APIEndpoint
import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.GitController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.secure
import spark.route.HttpMethod

object GitRouteConfig : RouteConfig
{
    private val runReports = setOf("*/reports.run")
    private val controller = GitController::class

    override val endpoints: List<EndpointDefinition> = listOf(
            APIEndpoint("/git/status/", controller, "status")
                    .json()
                    .secure(runReports),

            APIEndpoint("/git/pull/", controller, "pull", method = HttpMethod.post)
                    .json()
                    .secure(runReports),

            APIEndpoint("/git/fetch/", controller, "fetch", method = HttpMethod.post)
                    .json()
                    .secure(runReports),

            // deprecated
            APIEndpoint("/reports/git/status/", controller, "status")
                    .json()
                    .secure(runReports),
            // deprecated
            APIEndpoint("/reports/git/pull/", controller, "pull", method = HttpMethod.post)
                    .json()
                    .secure(runReports),
            // deprecated
            APIEndpoint("/reports/git/fetch/", controller, "fetch", method = HttpMethod.post)
                    .json()
                    .secure(runReports)
    )
}