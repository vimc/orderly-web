package org.vaccineimpact.orderlyweb.app_start

import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.Routing.*
import org.vaccineimpact.orderlyweb.controllers.web.HomeController
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
            WebEndpoint("/", HomeController::class, "index")
                    .secure(),
            WebEndpoint("/weblogin", SecurityController::class, "weblogin"),
            WebEndpoint("/weblogin/github", HomeController::class, "index", secureGithub = true) //TODO: This should redirect to requested endpoint

    )
}