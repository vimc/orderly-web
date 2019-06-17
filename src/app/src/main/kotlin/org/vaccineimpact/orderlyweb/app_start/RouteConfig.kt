package org.vaccineimpact.orderlyweb.app_start

import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.routing.api.*
import org.vaccineimpact.orderlyweb.app_start.routing.web.WebVersionRouteConfig
import org.vaccineimpact.orderlyweb.app_start.routing.web.WebReportRouteConfig
import org.vaccineimpact.orderlyweb.app_start.routing.web.WebUserGroupRouteConfig
import org.vaccineimpact.orderlyweb.app_start.routing.web.WebUserRouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.IndexController
import org.vaccineimpact.orderlyweb.controllers.web.SecurityController
import org.vaccineimpact.orderlyweb.secure

interface RouteConfig
{
    val endpoints: List<EndpointDefinition>
}

object APIRouteConfig : RouteConfig
{
    override val endpoints: List<EndpointDefinition> =
            ReportRouteConfig.endpoints
                    .plus(VersionRouteConfig.endpoints)
                    .plus(GitRouteConfig.endpoints)
                    .plus(HomeRouteConfig.endpoints)
                    .plus(DataRouteConfig.endpoints)
                    .plus(UserRouteConfig.endpoints)
}

object WebRouteConfig : RouteConfig
{
    override val endpoints: List<EndpointDefinition> = listOf(
            WebEndpoint("/", IndexController::class, "index")
                    .secure(setOf("*/reports.read")),
            WebEndpoint("/reports/", IndexController::class, "index")
                    .secure(setOf("*/reports.read")),
            WebEndpoint("/weblogin", SecurityController::class, "weblogin"),
            WebEndpoint("/weblogin/external", SecurityController::class, "webloginExternal")
                    .secure(externalAuth = true)
            ) +
            WebReportRouteConfig.endpoints +
            WebVersionRouteConfig.endpoints +
            WebUserRouteConfig.endpoints +
            WebUserGroupRouteConfig.endpoints

}