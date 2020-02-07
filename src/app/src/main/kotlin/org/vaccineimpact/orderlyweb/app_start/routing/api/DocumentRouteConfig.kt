package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.DocumentController
import spark.route.HttpMethod

object DocumentRouteConfig : RouteConfig
{
    //TODO: Should have a documents.manage permission instead?
    private val refreshPermission = setOf("*/users.manage")
    private val controller = DocumentController::class

    override val endpoints: List<EndpointDefinition> = listOf(
            APIEndpoint("/documents/refresh/", controller, "refreshDocuments", method = HttpMethod.post)
                    .secure(refreshPermission)
    )
}