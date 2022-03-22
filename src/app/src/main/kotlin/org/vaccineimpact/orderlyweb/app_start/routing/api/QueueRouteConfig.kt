package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.APIEndpoint
import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.QueueController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.secure

object QueueRouteConfig : RouteConfig
{
    private val controller = QueueController::class

    override val endpoints: List<EndpointDefinition> = listOf(
            APIEndpoint("/queue/status/", controller, "getStatus")
                    .secure()
                    .json()
    )
}
