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
                     val authRepo : AuthorizationRepository,
                     val userRepo: UserRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, OrderlyAuthorizationRepository(), OrderlyUserRepository())

    fun associatePermission(): String
    {
        val userEmail = userEmail()

        //check that this is a user and not a group
        if (userRepo.getUser(userEmail) == null)
        {
            throw UnknownObjectError(userEmail, "user")
        }

        val postData = context.postData()
        val associatePermission = AssociatePermission(
                postData["action"]!!,
                postData["name"]!!,
                postData["scope_prefix"],
                postData["scope_id"]
        )

        val permission = ReifiedPermission(associatePermission.name, Scope.parse(associatePermission))

        when (associatePermission.action)
        {
            "add" -> authRepo.ensureUserGroupHasPermission(userEmail, permission)
            "remove" -> authRepo.ensureUserGroupDoesNotHavePermission(userEmail, permission)
            else -> throw IllegalArgumentException("Unknown action type")
        }

        return okayResponse()
    }

    fun getReportReaders(): List<ReportReaderViewModel>
    {
        val report = report()
        val users = authRepo.getReportReaders(report)
        return users.map{ ReportReaderViewModel.build(it.key, it.value) }.sortedBy { it.username }
    }

    private fun userEmail(): String = URLDecoder.decode(context.params(":email"), "UTF-8")
    private fun report(): String = context.params(":report")
}