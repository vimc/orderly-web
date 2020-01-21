package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.UserController
import org.vaccineimpact.orderlyweb.controllers.web.UserGroupController
import spark.route.HttpMethod

object WebUserRouteConfig : RouteConfig
{
    private val usersManage = setOf("*/users.manage")
    override val endpoints = listOf(
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
            WebEndpoint("/user-groups/:user-group-id/actions/associate-permission/",
                    UserGroupController::class, "associatePermission",
                    method = HttpMethod.post)
                    .json()
                    .transform()
                    .secure(usersManage),
            WebEndpoint("/user-groups/:user-group-id/",
                    UserGroupController::class, "addUser",
                    method = HttpMethod.post)
                    .json()
                    .transform()
                    .secure(usersManage),
            WebEndpoint("/user-groups/:user-group-id/user/:email",
                    UserGroupController::class, "removeUser",
                    method = HttpMethod.delete)
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