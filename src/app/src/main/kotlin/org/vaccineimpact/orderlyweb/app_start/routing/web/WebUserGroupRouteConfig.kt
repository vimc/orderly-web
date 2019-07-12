package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.UserGroupController
import spark.route.HttpMethod

object WebUserGroupRouteConfig : RouteConfig
{
    private val usersManage = setOf("*/users.manage")
    override val endpoints = listOf(
            WebEndpoint("/user-groups/report-readers/",
                    UserGroupController::class, "getGlobalReportReaders")
                    .json()
                    .secure(usersManage)
                    .transform(),
            WebEndpoint("/user-groups/report-readers/:report/",
                    UserGroupController::class, "getScopedReportReaders")
                    .json()
                    .secure(usersManage + setOf("*/reports.read"))
                    .transform(),
            WebEndpoint("/user-groups/:user-group-id/actions/associate-permission/",
                    UserGroupController::class, "associatePermission",
                    method = HttpMethod.post)
                    .json()
                    .secure(usersManage),
            WebEndpoint("/typeahead/roles/",
                    UserGroupController::class, "getAllRoleNames")
                    .json()
                    .transform()
                    .secure(usersManage)

    )
}
