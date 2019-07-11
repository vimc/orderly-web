package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.UserController
import spark.route.HttpMethod

object WebUserRouteConfig : RouteConfig
{
    private val usersManage = setOf("*/users.manage")
    override val endpoints = listOf(
            WebEndpoint("/users/report-readers/:report/",
                UserController::class, "getScopedReportReaders",
                    contentType = ContentTypes.json)
                .json()
                .transform()
                .secure(usersManage),
            WebEndpoint("/emails/",
                    UserController::class, "getUserEmails",
                    contentType = ContentTypes.json)
                    .json()
                    .transform()
                    .secure(usersManage)
    )
}