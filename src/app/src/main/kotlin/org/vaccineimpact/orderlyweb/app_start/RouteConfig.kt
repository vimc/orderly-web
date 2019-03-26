package org.vaccineimpact.orderlyweb.app_start

import org.vaccineimpact.orderlyweb.Endpoint
import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.app_start.Routing.*
import org.vaccineimpact.orderlyweb.controllers.web.HomeController
import org.vaccineimpact.orderlyweb.html

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
            Endpoint("/", HomeController::class, "index")
                    .html()
    )
}