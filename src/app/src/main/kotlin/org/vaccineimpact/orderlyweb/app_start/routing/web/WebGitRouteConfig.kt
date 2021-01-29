package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.GitController

object WebGitRouteConfig : RouteConfig
{
    private val runReports = setOf("*/reports.run")
    override val endpoints: List<EndpointDefinition> = listOf(
            WebEndpoint("/git/branch/:branch/commits/",
                   GitController::class, "getCommits")
                    .secure(runReports)
                    .json(),

            WebEndpoint("/git/status/", GitController::class, "status")
                    .secure(runReports)
                    .json(),

            WebEndpoint("/git/pull/", GitController::class, "pull", method = HttpMethod.post)
                    .secure(runReports)
                    .json(),

            WebEndpoint("/git/fetch/", GitController::class, "fetch", method = HttpMethod.post)
                    .secure(runReports)
                    .json()
    )
}