package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.OutpackController

object OutpackRouteConfig : RouteConfig
{
    private val readReports = setOf("*/reports.read")
    private val controller = OutpackController::class

    override val endpoints: List<EndpointDefinition> = listOf(
            // route for all non-json GET requests
            APIEndpoint("/outpack/*/", controller, "get")
                    .secure(readReports),
            // route for all json GET requests
            APIEndpoint("/outpack/*/", controller, "get")
                    .json()
                    .secure(readReports)
    )
}
