package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.OutpackController

object OutpackRouteConfig : RouteConfig
{
    private val readReports = setOf("*/reports.read")
    private val controller = OutpackController::class

    override val endpoints: List<EndpointDefinition> = listOf(
            APIEndpoint("/outpack/", controller, "index")
                    .json()
                    .secure(readReports)
    )
}
