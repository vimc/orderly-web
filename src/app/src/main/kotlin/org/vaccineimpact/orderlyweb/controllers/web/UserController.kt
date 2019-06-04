package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.AssociatePermission
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.viewmodels.ReportReaderViewModel

class UserController(context: ActionContext,
                     val authRepo : AuthorizationRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, OrderlyAuthorizationRepository())

    fun associatePermission(): String
    {
        val userEmail = userEmail()
        val postData = context.postData()
        val associatePermission = AssociatePermission(
                postData["action"]!!,
                postData["name"]!!,
                postData["scope_prefix"],
                postData["scope_id"]
        )

        //TODO: Add check that this is a user and not a group

        val permission = ReifiedPermission(associatePermission.name, Scope.parse(associatePermission))

        if (associatePermission.action == "add")
        {
            authRepo.ensureUserGroupHasPermission(userEmail, permission)
            return okayResponse()
        }
        else
            throw IllegalArgumentException("Unknown action type")
    }

    fun getReportReaders(): List<ReportReaderViewModel>
    {
        val report = report()
        val users = authRepo.getReportReaders(report)
        return users.map{ ReportReaderViewModel.build(it.key, it.value) }.sortedBy { it.username }
    }

    private fun userEmail(): String = context.params(":email")
    private fun report(): String = context.params(":report")
}