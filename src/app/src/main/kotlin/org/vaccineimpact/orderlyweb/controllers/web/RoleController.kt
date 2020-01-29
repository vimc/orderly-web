package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.*
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.AssociatePermission
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.models.permissions.Role
import org.vaccineimpact.orderlyweb.permissionFromPostData
import org.vaccineimpact.orderlyweb.permissionFromRouteParams
import org.vaccineimpact.orderlyweb.viewmodels.RoleViewModel

class RoleController(context: ActionContext,
                     private val roleRepo: RoleRepository,
                     private val authRepo: AuthorizationRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, OrderlyRoleRepository(), OrderlyAuthorizationRepository())

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

    fun addRole(): String {
        val name = context.postData()["name"] ?: throw MissingParameterError("name")
        authRepo.createUserGroup(name)
        return okayResponse()
    }

    fun addPermission(): String
    {
        val roleId = roleId()
        val permission = context.permissionFromPostData()
        authRepo.ensureUserGroupHasPermission(roleId, permission)

        return okayResponse()
    }

    fun removePermission(): String
    {
        val roleId = roleId()
        val permission = context.permissionFromRouteParams()
        authRepo.ensureUserGroupDoesNotHavePermission(roleId, permission)

        return okayResponse()
    }

    fun addUser(): String
    {
        val roleId = roleId()
        val email = context.postData()["email"] ?: throw MissingParameterError("action")
        authRepo.ensureGroupHasMember(roleId, email)
        return okayResponse()
    }

    fun removeUser(): String
    {
        val roleId = roleId()
        val email = context.params(":email")
        authRepo.ensureGroupDoesNotHaveMember(roleId, email)
        return okayResponse()
    }

    private fun roleId(): String = context.params(":role-id")

    private fun List<Role>.toSortedViewModels() : List<RoleViewModel>
    {
        return this.map { RoleViewModel.build(it) }
                .sortedBy{ it.name }
    }
}