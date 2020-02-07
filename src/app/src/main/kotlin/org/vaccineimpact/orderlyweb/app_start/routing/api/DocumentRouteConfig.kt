package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.DocumentController
import spark.route.HttpMethod

object DocumentRouteConfig : RouteConfig
{
    private val controller = DocumentController::class

    override val endpoints: List<EndpointDefinition> = listOf(
            APIEndpoint("/documents/refresh/", controller, "refreshDocuments")
                    .post()
                    .json()
    )
}