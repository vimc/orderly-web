package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.RoleController
import org.vaccineimpact.orderlyweb.controllers.web.UserGroupController

object WebRoleRouteConfig : RouteConfig
{
    private val usersManage = setOf("*/users.manage")
    override val endpoints = listOf(
            WebEndpoint("/roles/",
                    RoleController::class, "getAll")
                    .json()
                    .secure(usersManage)
                    .transform(),
            WebEndpoint("/roles/report-readers/",
                    RoleController::class, "getGlobalReportReaders")
                    .json()
                    .secure(usersManage)
                    .transform(),
            WebEndpoint("/roles/report-readers/:report/",
                    RoleController::class, "getScopedReportReaders")
                    .json()
                    .secure(usersManage + setOf("*/reports.read"))
                    .transform(),
            WebEndpoint("/typeahead/roles/",
                    RoleController::class, "getAllRoleNames")
                    .json()
                    .transform()
                    .secure(usersManage)

    )
}
