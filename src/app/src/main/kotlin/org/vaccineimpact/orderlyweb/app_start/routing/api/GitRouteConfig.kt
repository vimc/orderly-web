package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.GitController
import org.vaccineimpact.orderlyweb.json
import spark.route.HttpMethod

object GitRouteConfig : RouteConfig {
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
                    .secure(runReports)
    )

}