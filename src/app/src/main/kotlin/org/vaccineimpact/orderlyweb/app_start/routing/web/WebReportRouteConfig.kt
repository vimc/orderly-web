package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.ReportRunController
import org.vaccineimpact.orderlyweb.controllers.web.IndexController
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.controllers.web.WorkflowRunController
import spark.route.HttpMethod

object WebReportRouteConfig : RouteConfig
{
    private val readReports = setOf("report:<name>/reports.read")
    private val runReports = setOf("*/reports.run")
    private val reviewReports = setOf("*/reports.review")
    private val configureReports = setOf("*/pinned-reports.manage")

    override val endpoints = listOf(
            WebEndpoint("/", IndexController::class, "index")
                    // more specific permission checking in the controller action
                    .secure(),
            WebEndpoint("/global-pinned-reports/",
                    ReportController::class, "setGlobalPinnedReports",
                    method = HttpMethod.post)
                    .json()
                    .secure(configureReports)
                    .transform(),
            WebEndpoint("/report/:name/latest/",
                    ReportRunController::class, "getLatestReportVersion")
                    .secure(readReports),
            WebEndpoint("/report/:name/:version/",
                    ReportController::class, "getByNameAndVersion")
                    .secure(readReports),
            WebEndpoint("/report/:name/actions/run/",
                    ReportRunController::class, "run",
                    method = HttpMethod.post)
                    .json()
                    .secure(runReports),
            WebEndpoint("/report/:name/actions/status/:key/",
                    ReportRunController::class, "status")
                    .json()
                    .secure(runReports),
            WebEndpoint(
                    "/run-report/",
                    ReportController::class, "getRunReport")
                    .secure(runReports),
            WebEndpoint(
                    "/run-workflow/",
                    WorkflowRunController::class, "getRunWorkflow")
                    .secure(runReports),
            WebEndpoint(
                    "/reports/runnable/",
                    ReportController::class, "getRunnableReports")
                    .json()
                    .secure(runReports)
                    .transform(),
            WebEndpoint(
                    "/report/:name/config/parameters/",
                    ReportController::class, "getReportParameters")
                    .json()
                    .secure(runReports)
                    .transform(),
            WebEndpoint("/running/:key/logs/",
                    org.vaccineimpact.orderlyweb.controllers.web.ReportRunController::class, "getRunningReportLogs")
                    .json()
                    .secure(runReports)
                    .transform(),
            WebEndpoint(
                    "/publish-reports/",
                    ReportController::class, "getPublishReports")
                    .secure(reviewReports),
            WebEndpoint(
                    "/report-drafts/",
                    ReportController::class, "getDrafts")
                    .json()
                    .transform()
                    .secure(reviewReports),
            WebEndpoint(
                    "/bulk-publish/",
                    ReportController::class, "publishReports")
                    .post()
                    .json()
                    .secure(reviewReports),
            WebEndpoint("/report/:name/dependencies/",
                    ReportController::class, "getDependencies")
                    .json()
                    .secure(readReports),
            WebEndpoint("/report/run-metadata",
                    ReportController::class, "getRunMetadata")
                    .json()
                    .transform()
                    .secure(runReports)
    )
}
