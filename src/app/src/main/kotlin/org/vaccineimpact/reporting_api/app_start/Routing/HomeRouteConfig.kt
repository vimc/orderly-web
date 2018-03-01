package org.vaccineimpact.reporting_api.app_start.Routing

import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.app_start.RouteConfig
import org.vaccineimpact.reporting_api.controllers.HomeController
import org.vaccineimpact.reporting_api.controllers.OnetimeTokenController

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