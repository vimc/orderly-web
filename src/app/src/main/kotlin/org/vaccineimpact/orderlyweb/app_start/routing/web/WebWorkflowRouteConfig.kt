package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.WorkflowRunController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.secure

object WebWorkflowRouteConfig : RouteConfig
{
    private val runReports = setOf("*/reports.run")

    override val endpoints: List<EndpointDefinition> = listOf(
        WebEndpoint("/workflows", WorkflowRunController::class, "getWorkflowRunSummaries")
            .secure(runReports)
            .json()
    )
}
