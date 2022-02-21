package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.WorkflowRunController
import spark.route.HttpMethod

object WebWorkflowRouteConfig : RouteConfig
{
    private val runReports = setOf("*/reports.run")

    override val endpoints: List<EndpointDefinition> = listOf(
            WebEndpoint("/workflow", WorkflowRunController::class, "createWorkflowRun", HttpMethod.post)
                    .json()
                    .secure(runReports),
            WebEndpoint("/workflows", WorkflowRunController::class, "getWorkflowRunSummaries")
                    .json()
                    .secure(runReports)
                    .transform(),
            WebEndpoint("/workflows/summary", WorkflowRunController::class, "getWorkflowSummary", HttpMethod.post)
                    .json()
                    .transform()
                    .secure(runReports),
            WebEndpoint("/workflows/:key", WorkflowRunController::class, "getWorkflowRunDetails")
                    .json()
                    .secure(runReports)
                    .transform(),
            WebEndpoint("/workflows/:key/status", WorkflowRunController::class, "getWorkflowRunStatus")
                    .json()
                    .secure(runReports)
                    .transform(),
            WebEndpoint("/workflow/validate", WorkflowRunController::class, "validateWorkflow", HttpMethod.post)
                    .json()
                    .secure(runReports)
                    .transform()
    )
}
