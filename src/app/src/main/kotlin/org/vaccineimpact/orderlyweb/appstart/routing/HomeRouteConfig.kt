package org.vaccineimpact.orderlyweb.appstart.routing

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.appstart.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.HomeController
import org.vaccineimpact.orderlyweb.controllers.api.OnetimeTokenController

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