package org.vaccineimpact.orderlyweb.appstart

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.appstart.routing.*
import org.vaccineimpact.orderlyweb.controllers.web.AdminController
import org.vaccineimpact.orderlyweb.controllers.web.HomeController
import org.vaccineimpact.orderlyweb.controllers.web.LoginController
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import spark.route.HttpMethod

interface RouteConfig
{
    val endpoints: List<EndpointDefinition>
}

object ApiRouteConfig : RouteConfig
{
    override val endpoints: List<EndpointDefinition> =
            ReportRouteConfig.endpoints
                    .plus(VersionRouteConfig.endpoints)
                    .plus(GitRouteConfig.endpoints)
                    .plus(HomeRouteConfig.endpoints)
                    .plus(DataRouteConfig.endpoints)
}

object WebRouteConfig : RouteConfig
{
    override val endpoints = listOf(
            Endpoint("/", HomeController::class, "get")
                    .html()
                    .secure(),
            Endpoint("/login", LoginController::class, "get")
                    .html(),
            Endpoint("/admin", AdminController::class, "get")
                    .html(),
            Endpoint("/admin/adduser", AdminController::class, "post")
                    .copy(method = HttpMethod.post),
            Endpoint("/reports", ReportController::class, "getAll")
                    .html(),
            Endpoint("/reports/:name/versions/:version", ReportController::class, "get")
                    .html()
    )
}