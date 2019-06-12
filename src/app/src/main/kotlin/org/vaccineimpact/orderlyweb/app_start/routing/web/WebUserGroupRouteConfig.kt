package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.UserGroupController
import spark.route.HttpMethod

object WebUserGroupRouteConfig : RouteConfig
{
    private val usersManage = setOf("*/users.manage")
    override val endpoints = listOf(
            WebEndpoint("/user-groups/:user-group-id/actions/associate-permission/",
                    UserGroupController::class, "associatePermission",
                    contentType = ContentTypes.json,
                    method = HttpMethod.post)
                    .json()
                    .secure(usersManage)
    )
}