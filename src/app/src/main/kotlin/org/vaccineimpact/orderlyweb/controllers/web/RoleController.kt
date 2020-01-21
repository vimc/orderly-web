package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.OrderlyRoleRepository
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.RoleRepository
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.models.permissions.Role
import org.vaccineimpact.orderlyweb.viewmodels.RoleViewModel

class RoleController(context: ActionContext,
                     private val roleRepo: RoleRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, OrderlyRoleRepository())

    fun getGlobalReportReaders(): List<RoleViewModel>
    {
        return roleRepo.getGlobalReportReaderRoles()
                .toSortedViewModels()
    }

    fun getScopedReportReaders(): List<RoleViewModel>
    {
        return roleRepo.getScopedReportReaderRoles(context.params(":report"))
                .toSortedViewModels()
    }

    fun getAll(): List<RoleViewModel>
    {
        return roleRepo.getAllRoles()
                .toSortedViewModels()
    }

    fun getAllRoleNames(): List<String>
    {
        return roleRepo.getAllRoleNames()
    }

    private fun List<Role>.toSortedViewModels() : List<RoleViewModel>
    {
        return this.map { RoleViewModel.build(it) }
                .sortedBy{ it.name }
    }
}