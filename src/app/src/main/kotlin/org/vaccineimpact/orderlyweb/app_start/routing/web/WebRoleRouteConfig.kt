package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.RoleController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.secure
import org.vaccineimpact.orderlyweb.transform
import spark.route.HttpMethod

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
                    .secure(usersManage),
            WebEndpoint("/roles/",
                    RoleController::class, "addRole",
                    method = HttpMethod.post)
                    .json()
                    .transform()
                    .secure(usersManage),
            WebEndpoint("/roles/:role-id/users/",
                    RoleController::class, "addUser",
                    method = HttpMethod.post)
                    .json()
                    .transform()
                    .secure(usersManage),
            WebEndpoint("/roles/:role-id/users/:email",
                    RoleController::class, "removeUser",
                    method = HttpMethod.delete)
                    .json()
                    .transform()
                    .secure(usersManage),
            WebEndpoint("/roles/:role-id/actions/associate-permission/",
                    RoleController::class, "associatePermission",
                    method = HttpMethod.post)
                    .json()
                    .transform()
                    .secure(usersManage)

    )
}
