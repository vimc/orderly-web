package org.vaccineimpact.orderlyweb.controllers.web

import java.net.URLDecoder
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.AssociatePermission
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.viewmodels.ReportReaderViewModel

class UserController(context: ActionContext,
                     val authRepo : AuthorizationRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, OrderlyAuthorizationRepository())

    fun getReportReaders(): List<ReportReaderViewModel>
    {
        val report = report()
        val users = authRepo.getReportReaders(report)
        return users.map { ReportReaderViewModel.build(it.key, it.value) }
                .sortedBy { it.displayName.toLowerCase() }
    }

    private fun report(): String = context.params(":report")
}