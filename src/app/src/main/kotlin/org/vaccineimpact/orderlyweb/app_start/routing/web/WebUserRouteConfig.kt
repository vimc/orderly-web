package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.UserController
import org.vaccineimpact.orderlyweb.controllers.web.AdminController
import spark.route.HttpMethod

object WebUserRouteConfig : RouteConfig
{
    private val usersManage = setOf("*/users.manage")
    override val endpoints = listOf(
            WebEndpoint("/users/",
                    UserController::class, "getAllUsers")
                    .json()
                    .transform()
                    .secure(usersManage),
            WebEndpoint("/users/report-readers/",
                    UserController::class, "getGlobalReportReaders")
                    .json()
                    .transform()
                    .secure(usersManage),
            WebEndpoint("/users/report-readers/:report/",
                    UserController::class, "getScopedReportReaders",
                    contentType = ContentTypes.json)
                    .json()
                    .transform()
                    .secure(usersManage),
            WebEndpoint("/users/:user-id/actions/associate-permission/",
                    AdminController::class, "associatePermission",
                    method = HttpMethod.post)
                    .json()
                    .transform()
                    .secure(usersManage),
            WebEndpoint("/typeahead/emails/",
                    UserController::class, "getUserEmails",
                    contentType = ContentTypes.json)
                    .json()
                    .transform()
                    .secure(usersManage)
    )
}