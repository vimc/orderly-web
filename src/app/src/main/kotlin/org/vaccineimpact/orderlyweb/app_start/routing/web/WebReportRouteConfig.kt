package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.ArtefactController
import org.vaccineimpact.orderlyweb.controllers.api.ResourceController
import org.vaccineimpact.orderlyweb.controllers.api.VersionController
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.secure
import spark.route.HttpMethod

object WebReportRouteConfig : RouteConfig
{
    private val readReports = setOf("report:<name>/reports.read")
    private val reviewReports = setOf("*/reports.review")
    override val endpoints = listOf(
            WebEndpoint("/reports/:name/:version/",
                    ReportController::class, "getByNameAndVersion")
                    .secure(readReports),
            WebEndpoint("/reports/:name/versions/:version/artefacts/:artefact/",
                    ArtefactController::class, "getFile")
                    .secure(readReports),
            WebEndpoint("/reports/:name/versions/:version/resources/:resource/",
                    ResourceController::class, "download")
                    .secure(readReports),
            WebEndpoint("/reports/:name/versions/:version/all/",
                    VersionController::class, "getZippedByNameAndVersion")
                    .secure(readReports),
            WebEndpoint("/reports/:name/versions/:version/publish/",
                    org.vaccineimpact.orderlyweb.controllers.api.ReportController::class, "publish",
                    method = HttpMethod.post)
                    .json()
                    .secure(reviewReports)
    )
}