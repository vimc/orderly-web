package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.GitController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.secure

object WebGitRouteConfig : RouteConfig
{
    private val runReports = setOf("*/reports.run")
    override val endpoints: List<EndpointDefinition> = listOf(
            WebEndpoint("/git/branch/:branch/commits/",
                    GitController::class, "getCommits")
                    .secure(runReports)
                    .json(),

            WebEndpoint("/git/fetch/",
                    GitController::class, "fetch")
                    .secure(runReports)
                    .json()
    )
}