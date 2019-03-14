package org.vaccineimpact.orderlyweb.app_start.Routing

import org.vaccineimpact.orderlyweb.Endpoint
import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.UserController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.post
import org.vaccineimpact.orderlyweb.transform
import org.vaccineimpact.orderlyweb.basicAuth

object UserRouteConfig: RouteConfig {
    override val endpoints: List<EndpointDefinition> = listOf(
            Endpoint("/login/", UserController::class, "basicAuth")
                    .post()
                    .json()
                    .basicAuth()
    )
}