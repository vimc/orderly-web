package org.vaccineimpact.orderlyweb.app_start

import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.routing.api.*
import org.vaccineimpact.orderlyweb.app_start.routing.web.WebDataRouteConfig
import org.vaccineimpact.orderlyweb.app_start.routing.web.WebReportRouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.HomeController
import org.vaccineimpact.orderlyweb.secure

interface RouteConfig
{
    val endpoints: List<EndpointDefinition>
}

object APIRouteConfig : RouteConfig
{
    override val endpoints: List<EndpointDefinition> =
            ReportRouteConfig.endpoints
                    .plus(VersionRouteConfig.endpoints)
                    .plus(GitRouteConfig.endpoints)
                    .plus(HomeRouteConfig.endpoints)
                    .plus(DataRouteConfig.endpoints)
                    .plus(UserRouteConfig.endpoints)
}

object WebRouteConfig : RouteConfig
{
    override val endpoints: List<EndpointDefinition> = listOf(
            WebEndpoint("/", HomeController::class, "index")
                    .secure()
    ) +
            WebReportRouteConfig.endpoints +
            WebDataRouteConfig.endpoints
}