package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.UserController

object UserRouteConfig : RouteConfig
{
    private val usersManage = setOf("*/users.manage")
    override val endpoints: List<EndpointDefinition> = listOf(
            APIEndpoint("/login/", UserController::class, "auth")
                    .post()
                    .json()
                    .secure()
                    .transform()
                    .externalAuth(),

            APIEndpoint("/user/add/", UserController::class, "addUser")
                    .post()
                    .json()
                    .secure(usersManage)
    )
}