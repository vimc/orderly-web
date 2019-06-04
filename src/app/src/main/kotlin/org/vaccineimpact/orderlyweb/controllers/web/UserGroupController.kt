package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.AssociatePermission
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

class UserGroupController(context: ActionContext,
                          val authRepo : AuthorizationRepository = OrderlyAuthorizationRepository()) : Controller(context)
{
    fun associatePermission()
    {
        val userGroupId = userGroupId()
        val postData = context.postData()
        val associatePermission = AssociatePermission(
                postData["action"]!!,
                postData["name"]!!,
                postData["scope_prefix"],
                postData["scope_id"]
        )

        val permission = ReifiedPermission(associatePermission.name, Scope.parse(associatePermission))

        if (associatePermission.action == "add")
            authRepo.ensureUserGroupHasPermission(userGroupId, permission)
        else
            throw IllegalArgumentException("Unknown action type")
    }

    private fun userGroupId(): String = context.params(":user-group-id")
}