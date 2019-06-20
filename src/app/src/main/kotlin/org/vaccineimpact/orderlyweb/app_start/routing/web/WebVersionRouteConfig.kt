package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.ArtefactController
import org.vaccineimpact.orderlyweb.controllers.api.DataController
import org.vaccineimpact.orderlyweb.controllers.api.ResourceController
import org.vaccineimpact.orderlyweb.controllers.api.VersionController
import spark.route.HttpMethod

object WebVersionRouteConfig : RouteConfig
{
    private val readReports = setOf("report:<name>/reports.read")
    private val reviewReports = setOf("*/reports.review")

    override val endpoints: List<EndpointDefinition> = listOf(
            WebEndpoint("/report/:name/version/:version/artefacts/:artefact/",
                    ArtefactController::class, "getFile", contentType = ContentTypes.binarydata)
                    .secure(readReports),
            WebEndpoint("/report/:name/version/:version/resources/:resource/",
                    ResourceController::class, "download", contentType = ContentTypes.binarydata)
                    .secure(readReports),
            WebEndpoint("/report/:name/version/:version/all/",
                    VersionController::class, "getZippedByNameAndVersion", contentType = ContentTypes.binarydata)
                    .secure(readReports),
            WebEndpoint("/report/:name/version/:version/publish/",
                    org.vaccineimpact.orderlyweb.controllers.api.ReportController::class, "publish",
                    method = HttpMethod.post)
                    .json()
                    .secure(reviewReports),
            WebEndpoint("/report/:name/version/:version/data/:data/",
                    org.vaccineimpact.orderlyweb.controllers.api.DataController::class, "downloadData",
                    contentType = ContentTypes.csv)
                    .secure(readReports),
            WebEndpoint("/reports/:name/versions/:version/data/:data/",
                    org.vaccineimpact.orderlyweb.controllers.api.DataController::class, "downloadData",
                    contentType = ContentTypes.binarydata)
                    .secure(readReports)

    )


}