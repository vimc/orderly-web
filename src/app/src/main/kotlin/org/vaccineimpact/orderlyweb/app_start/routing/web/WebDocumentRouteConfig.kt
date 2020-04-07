package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.DocumentController

object WebDocumentRouteConfig : RouteConfig
{
    private val readDocuments = setOf("*/documents.read")
    private val manageDocuments = setOf("*/documents.manage")
    override val endpoints: List<EndpointDefinition> = listOf(
            WebEndpoint("/project-docs/*",
                    DocumentController::class, "getDocument", contentType = ContentTypes.binarydata)
                    .secure(readDocuments),
            WebEndpoint("/project-docs/",
                    DocumentController::class, "getAll")
                    .secure(readDocuments),
            WebEndpoint("/documents/refresh/", DocumentController::class, "refreshDocuments")
                    .post()
                    .json()
                    .secure(manageDocuments)
    )

}