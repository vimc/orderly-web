package org.vaccineimpact.orderlyweb.appstart

import org.vaccineimpact.orderlyweb.Endpoint
import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.appstart.routing.*
import org.vaccineimpact.orderlyweb.controllers.web.HomeController
import org.vaccineimpact.orderlyweb.controllers.web.LoginController
import org.vaccineimpact.orderlyweb.html
import org.vaccineimpact.orderlyweb.secure

interface RouteConfig {
    val endpoints: List<EndpointDefinition>
}

object ApiRouteConfig : RouteConfig {
    override val endpoints: List<EndpointDefinition> =
            ReportRouteConfig.endpoints
                    .plus(VersionRouteConfig.endpoints)
                    .plus(GitRouteConfig.endpoints)
                    .plus(HomeRouteConfig.endpoints)
                    .plus(DataRouteConfig.endpoints)
}

object WebRouteConfig : RouteConfig {
    override val endpoints = listOf(
            Endpoint("/", HomeController::class, "get")
                    .html()
                    .secure(),
            Endpoint("/login", LoginController::class, "get")
                    .html()
    )
}