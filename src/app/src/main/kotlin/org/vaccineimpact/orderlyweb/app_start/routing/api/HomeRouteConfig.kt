package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.HomeController

object HomeRouteConfig : RouteConfig
{
    override val endpoints: List<EndpointDefinition> = listOf(
            APIEndpoint("/", HomeController::class, "index")
                    .json()
                    .transform()
    )
}
