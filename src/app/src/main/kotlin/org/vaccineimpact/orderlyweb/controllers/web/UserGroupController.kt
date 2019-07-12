package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.AssociatePermission
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

class UserGroupController(context: ActionContext,
                          private val authRepo: AuthorizationRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, OrderlyAuthorizationRepository())

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

    private fun userGroupId(): String = context.params(":user-group-id")
}