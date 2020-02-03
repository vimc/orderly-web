package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository

class PermissionController(actionContext: ActionContext,
                           private val authRepo: AuthorizationRepository) : Controller(actionContext)
{

    constructor(context: ActionContext) : this(context, OrderlyAuthorizationRepository())

    fun getPermissionNames(): List<String>
    {
        return authRepo.getPermissionNames()
    }
}