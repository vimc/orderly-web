package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.WorkflowRunController

object WebWorkflowRouteConfig : RouteConfig
{
    private val runReports = setOf("*/reports.run")

    override val endpoints: List<EndpointDefinition> = listOf(
            WebEndpoint("/workflows/:key", WorkflowRunController::class, "getRunWorkflowDetails")
                    .json()
                    .secure(runReports)
                    .transform()
    )
}