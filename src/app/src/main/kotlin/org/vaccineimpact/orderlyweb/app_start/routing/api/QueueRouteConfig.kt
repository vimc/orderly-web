package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.QueueController

object QueueRouteConfig : RouteConfig
{
    private val controller = QueueController::class

    override val endpoints: List<EndpointDefinition> = listOf(
            APIEndpoint("/queue/status/", controller, "getStatus")
                    .secure()
                    .json()
    )
}
