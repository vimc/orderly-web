package org.vaccineimpact.orderlyweb.app_start.Routing

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.HomeController
import org.vaccineimpact.orderlyweb.controllers.OnetimeTokenController

object HomeRouteConfig : RouteConfig
{
    override val endpoints: List<EndpointDefinition> = listOf(

            Endpoint("/onetime_token/", OnetimeTokenController::class, "get")
                    .json()
                    .secure()
                    .transform(),

            Endpoint("/", HomeController::class, "index")
                    .json()
                    .transform()
    )

}