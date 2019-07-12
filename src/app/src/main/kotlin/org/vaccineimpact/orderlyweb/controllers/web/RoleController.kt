package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.viewmodels.RoleViewModel

class RoleController(context: ActionContext,
                     private val userRepo: UserRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, OrderlyUserRepository())

    fun getGlobalReportReaders(): List<RoleViewModel>
    {
        val users = userRepo.getGlobalReportReaderRoles()
        return users.map { RoleViewModel.build(it) }
                .sortedBy { it.name }
    }

    fun getScopedReportReaders(): List<RoleViewModel>
    {
        val users = userRepo.getScopedReportReaderRoles(context.params(":report"))
        return users.map { RoleViewModel.build(it) }
                .sortedBy { it.name }
    }

    fun getAllRoleNames(): List<String>
    {
        return userRepo.getAllRoleNames()
    }

    private fun userGroupId(): String = context.params(":user-group-id")
}