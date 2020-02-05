package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.DocumentController

object WebDocumentRouteConfig : RouteConfig
{
    override val endpoints: List<EndpointDefinition> = listOf(
            WebEndpoint("/documents/*",
                    DocumentController::class, "getDocument", contentType = ContentTypes.binarydata)
                    .secure())

}