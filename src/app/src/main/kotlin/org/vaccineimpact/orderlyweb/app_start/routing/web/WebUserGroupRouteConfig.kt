package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.UserGroupController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.secure
import spark.route.HttpMethod

object WebUserGroupRouteConfig : RouteConfig
{
    private val usersManage = setOf("*/users.manage")
    override val endpoints = listOf(
            WebEndpoint("/user-group/:user-group-id/actions/associate-permission/",
                    UserGroupController::class, "associatePermission",
                    method = HttpMethod.post)
                    .json()
                    .secure(usersManage)
            )
}