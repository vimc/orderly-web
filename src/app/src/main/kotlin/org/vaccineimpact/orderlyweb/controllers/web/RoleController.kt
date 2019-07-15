package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.OrderlyRoleRepository
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.RoleRepository
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.viewmodels.RoleViewModel

class RoleController(context: ActionContext,
                     private val roleRepo: RoleRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, OrderlyRoleRepository())

    fun getGlobalReportReaders(): List<RoleViewModel>
    {
        val users = roleRepo.getGlobalReportReaderRoles()
        return users.map { RoleViewModel.build(it) }
                .sortedBy { it.name }
    }

    fun getScopedReportReaders(): List<RoleViewModel>
    {
        val users = roleRepo.getScopedReportReaderRoles(context.params(":report"))
        return users.map { RoleViewModel.build(it) }
                .sortedBy { it.name }
    }

    fun getAllRoleNames(): List<String>
    {
        return roleRepo.getAllRoleNames()
    }

}