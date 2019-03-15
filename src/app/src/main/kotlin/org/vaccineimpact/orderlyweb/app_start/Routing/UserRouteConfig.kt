package org.vaccineimpact.orderlyweb.app_start.Routing

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.UserController

object UserRouteConfig: RouteConfig {
    override val endpoints: List<EndpointDefinition> = listOf(
            Endpoint("/login/", UserController::class, "githubAuth")
                    .post()
                    .json()
                    .secure()
                    .transform()
                    .githubAuth()
    )
}