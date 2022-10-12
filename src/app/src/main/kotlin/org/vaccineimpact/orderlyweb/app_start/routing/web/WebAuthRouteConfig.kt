package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.SecurityController
import org.vaccineimpact.orderlyweb.secure

object WebAuthRouteConfig : RouteConfig
{
    override val endpoints: List<EndpointDefinition> =
            listOf(
                    WebEndpoint("/weblogin", SecurityController::class, "weblogin"),
                    WebEndpoint("/weblogin/external", SecurityController::class, "webloginExternal")
                            .secure(externalAuth = true)
            )
}
