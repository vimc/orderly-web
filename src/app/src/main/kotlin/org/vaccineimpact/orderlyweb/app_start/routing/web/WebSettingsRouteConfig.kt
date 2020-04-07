package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.SettingsController
import spark.route.HttpMethod

object WebSettingsRouteConfig : RouteConfig
{
    private val usersManage = setOf("*/users.manage")
    override val endpoints: List<EndpointDefinition> = listOf(
                    WebEndpoint("/settings/auth-allow-guest/",
                            SettingsController::class, "getAuthAllowGuest")
                            .json()
                            .secure(usersManage)
                            .transform(),
                    WebEndpoint("/settings/auth-allow-guest/",
                            SettingsController::class, "setAuthAllowGuest",
                            method = HttpMethod.post)
                            .json()
                            .secure(usersManage)
                            .transform()
            )
}