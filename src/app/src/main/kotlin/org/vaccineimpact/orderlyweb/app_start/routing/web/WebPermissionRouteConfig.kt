package org.vaccineimpact.orderlyweb.app_start.routing.web

import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.web.PermissionController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.secure
import org.vaccineimpact.orderlyweb.transform

object WebPermissionRouteConfig : RouteConfig
{
    private val usersManage = setOf("*/users.manage")
    override val endpoints = listOf(
            WebEndpoint("/typeahead/permissions/",
                    PermissionController::class, "getPermissionNames")
                    .json()
                    .secure(usersManage)
                    .transform())
}
