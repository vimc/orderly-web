package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.UserController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.secure
import org.vaccineimpact.orderlyweb.transform
import spark.route.HttpMethod

object WebUserRouteConfig : RouteConfig
{
    private val usersManage = setOf("*/users.manage")
    override val endpoints = listOf(
            WebEndpoint("/users/:email/actions/associate-permission/",
                    UserController::class, "associatePermission",
                    method = HttpMethod.post)
                    .json()
                    .secure(usersManage),
            WebEndpoint("/users/report-readers/:report/",
                UserController::class, "getReportReaders")
                .json()
                .transform()
                .secure(usersManage)
    )
}