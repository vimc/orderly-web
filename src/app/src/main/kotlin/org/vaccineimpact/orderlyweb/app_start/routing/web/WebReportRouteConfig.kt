package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.IndexController
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import spark.route.HttpMethod

object WebReportRouteConfig : RouteConfig
{
    private val readReports = setOf("report:<name>/reports.read")
    private val reviewReports = setOf("*/reports.review")
    private val configureReports = setOf("*/pinned-reports.manage")

    override val endpoints = listOf(
            WebEndpoint(
                    "/",
                    IndexController::class, "index"
            )
                    // more specific permission checking in the controller action
                    .secure(),

            WebEndpoint(
                    "/global-pinned-reports/",
                    ReportController::class,
                    "setGlobalPinnedReports",
                    method = HttpMethod.post
            )
                    .json()
                    .secure(configureReports)
                    .transform(),

            WebEndpoint(
                    "/report/:name/latest/",
                    ReportController::class,
                    "getByNameAndVersion"
            )
                    .secure(readReports),

            WebEndpoint(
                    "/report/:name/:version/",
                    ReportController::class,
                    "getByNameAndVersion"
            )
                    .secure(readReports),


            WebEndpoint(
                    "/publish-reports/",
                    ReportController::class,
                    "getPublishReports"
            )
                    .secure(reviewReports),

            WebEndpoint(
                    "/report-drafts/",
                    ReportController::class,
                    "getDrafts"
            )
                    .json()
                    .transform()
                    .secure(reviewReports),

            WebEndpoint(
                    "/bulk-publish/",
                    ReportController::class, "publishReports"
            )
                    .post()
                    .json()
                    .secure(reviewReports),

            WebEndpoint(
                    "/report/:name/dependencies/",
                    ReportController::class,
                    "getDependencies"
            )
                    .json()
                    .secure(readReports)
    )
}
