package org.vaccineimpact.orderlyweb.appstart.routing

import org.vaccineimpact.orderlyweb.Endpoint
import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.appstart.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.GitController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.secure
import spark.route.HttpMethod

object GitRouteConfig : RouteConfig {
    private val runReports = setOf("*/reports.run")
    private val controller = GitController::class

    override val endpoints: List<EndpointDefinition> = listOf(
            Endpoint("/reports/git/status/", controller, "status")
                    .json()
                    .secure(runReports),

            Endpoint("/reports/git/pull/", controller, "pull", method = HttpMethod.post)
                    .json()
                    .secure(runReports),

            Endpoint("/reports/git/fetch/", controller, "fetch", method = HttpMethod.post)
                    .json()
                    .secure(runReports)
    )

}