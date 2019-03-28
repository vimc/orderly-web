package org.vaccineimpact.orderlyweb.app_start.Routing

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.UserController

object UserRouteConfig: RouteConfig {
    override val endpoints: List<EndpointDefinition> = listOf(
            APIEndpoint("/login/", UserController::class, "auth")
                    .post()
                    .json()
                    .secure()
                    .transform()
                    .githubAuth()
    )
}