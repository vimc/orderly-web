package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.AssociatePermission
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.viewmodels.AdminViewModel
import org.vaccineimpact.orderlyweb.viewmodels.ReportVersionPageViewModel

class AdminController(context: ActionContext,
                          private val authRepo: AuthorizationRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, OrderlyAuthorizationRepository())

    @Template("admin.ftl")
    fun admin(): AdminViewModel
    {
        return AdminViewModel(context)
    }

}