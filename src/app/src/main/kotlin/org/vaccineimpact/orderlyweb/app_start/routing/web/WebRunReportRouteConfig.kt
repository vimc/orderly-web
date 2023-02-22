package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.ReportRunController
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.controllers.web.WorkflowRunController
import spark.route.HttpMethod

object WebRunReportRouteConfig : RouteConfig
{
    private val runReports = setOf("*/reports.run")

    override val endpoints = listOf(
            WebEndpoint(
                    "/report/:name/actions/run/",
                    ReportRunController::class,
                    "run",
                    method = HttpMethod.post
            )
                    .json()
                    .secure(runReports),

            WebEndpoint(
                    "/report/:name/actions/status/:key/",
                    ReportRunController::class,
                    "status"
            )
                    .json()
                    .secure(runReports),

            WebEndpoint(
                    "/run-report/",
                    ReportController::class,
                    "getRunReport"
            )
                    .secure(runReports),

            WebEndpoint(
                    "/run-workflow/",
                    WorkflowRunController::class,
                    "getRunWorkflow"
            )
                    .secure(runReports),

            WebEndpoint(
                    "/vuex-run-report/",
                    org.vaccineimpact.orderlyweb.controllers.web.vuex.ReportController::class,
                    "getRunReport"
            )
                    .secure(runReports),

            WebEndpoint(
                    "/reports/runnable/",
                    ReportController::class,
                    "getRunnableReports"
            )
                    .json()
                    .secure(runReports)
                    .transform(),

            WebEndpoint(
                    "/report/:name/config/parameters/",
                    ReportController::class, "getReportParameters"
            )
                    .json()
                    .secure(runReports)
                    .transform(),

            WebEndpoint(
                    "/running/:key/logs/",
                    org.vaccineimpact.orderlyweb.controllers.web.ReportRunController::class,
                    "getRunningReportLogs"
            )
                    .json()
                    .secure(runReports)
                    .transform(),

            WebEndpoint(
                    "/report/run-metadata",
                    ReportController::class,
                    "getRunMetadata"
            )
                    .json()
                    .transform()
                    .secure(runReports)
    )
}
