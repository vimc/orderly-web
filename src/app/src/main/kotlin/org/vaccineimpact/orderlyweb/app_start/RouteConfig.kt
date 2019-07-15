package org.vaccineimpact.orderlyweb.app_start

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.routing.api.*
import org.vaccineimpact.orderlyweb.app_start.routing.web.*
import org.vaccineimpact.orderlyweb.controllers.web.IndexController

interface RouteConfig
{
    val endpoints: List<EndpointDefinition>
}

object APIRouteConfig : RouteConfig
{
    override val endpoints: List<EndpointDefinition> =
            GitRouteConfig.endpoints.plus(ReportRouteConfig.endpoints)
                    .plus(VersionRouteConfig.endpoints)
                    .plus(HomeRouteConfig.endpoints)
                    .plus(DataRouteConfig.endpoints)
                    .plus(UserRouteConfig.endpoints)
}

object WebRouteConfig : RouteConfig
{
    private val metricsEndpoint = WebEndpoint("/metrics/", IndexController::class, "metrics")
            .json()

    override val endpoints: List<EndpointDefinition> =
            WebAuthRouteConfig.endpoints +
                    WebReportRouteConfig.endpoints +
                    WebVersionRouteConfig.endpoints +
                    WebUserRouteConfig.endpoints +
                    WebRoleRouteConfig.endpoints + metricsEndpoint
}

