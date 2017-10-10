package org.vaccineimpact.reporting_api.app_start.Routing

import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.app_start.RouteConfig

object HomeRouteConfig: RouteConfig{
    override val endpoints: List<EndpointDefinition> = listOf(

            Endpoint("/onetime_token/", "OnetimeToken", "get")
                    .json()
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/", "Home", "index")
                    .json()
                    .transform()
    )

}