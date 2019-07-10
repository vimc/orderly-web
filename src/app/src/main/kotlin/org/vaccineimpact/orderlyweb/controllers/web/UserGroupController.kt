package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.AssociatePermission
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.viewmodels.UserGroupViewModel

class UserGroupController(context: ActionContext,
                          private val authRepo: AuthorizationRepository,
                          private val userRepo: UserRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, OrderlyAuthorizationRepository(),
            OrderlyUserRepository())

    fun associatePermission(): String
    {
        val userGroupId = userGroupId()

        val postData = context.postData()
        val associatePermission = AssociatePermission(
                postData["action"] ?: throw MissingParameterError("action"),
                postData["name"] ?: throw MissingParameterError("name"),
                postData["scope_prefix"],
                postData["scope_id"]
        )

        val permission = ReifiedPermission(associatePermission.name, Scope.parse(associatePermission))

        when (associatePermission.action)
        {
            "add" -> authRepo.ensureUserGroupHasPermission(userGroupId, permission)
            "remove" -> authRepo.ensureUserGroupDoesNotHavePermission(userGroupId, permission)
            else -> throw IllegalArgumentException("Unknown action type")
        }

        return okayResponse()
    }

    fun getGlobalReportReaders(): List<UserGroupViewModel>
    {
        val users = userRepo.getGlobalReportReaderGroups()
        return users.map { UserGroupViewModel.build(it, false) }
                .sortedBy { it.name }
    }

    fun getScopedReportReaders(): List<UserGroupViewModel>
    {
        val users = userRepo.getScopedReportReaderGroups(context.params(":name"))
        return users.map { UserGroupViewModel.build(it, true) }
                .sortedBy { it.name }
    }

    private fun userGroupId(): String = context.params(":user-group-id")
}