package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.IndexController
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.post
import org.vaccineimpact.orderlyweb.secure
import spark.route.HttpMethod

object WebReportRouteConfig : RouteConfig
{
    private val readReports = setOf("report:<name>/reports.read")
    private val runReports = setOf("*/reports.run")
    private val reviewReports = setOf("*/reports.review")
    override val endpoints = listOf(
            WebEndpoint("/", IndexController::class, "index")
                    // more specific permission checking in the controller action
                    .secure(),
            WebEndpoint("/report/:name/:version/",
                    ReportController::class, "getByNameAndVersion")
                    .secure(readReports),
            WebEndpoint("/report/:name/actions/run/",
                    org.vaccineimpact.orderlyweb.controllers.api.ReportController::class, "run",
                    method = HttpMethod.post)
                    .json()
                    .secure(runReports),
            WebEndpoint("/report/:name/actions/status/:key/",
                    org.vaccineimpact.orderlyweb.controllers.api.ReportController::class, "status")
                    .json()
                    .secure(runReports),
            WebEndpoint("/report/:name/tags/",
                    ReportController::class, "tagReport")
                    .json()
                    .post()
                    .secure(reviewReports),
            WebEndpoint("/report/:name/tag/:tag",
                    ReportController::class, "deleteReportTag",
                    method = HttpMethod.delete)
                    .json()
                    .secure(reviewReports)
    )
}