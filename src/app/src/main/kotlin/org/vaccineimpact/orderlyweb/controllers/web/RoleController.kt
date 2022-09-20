package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.repositories.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyRoleRepository
import org.vaccineimpact.orderlyweb.db.repositories.RoleRepository
import org.vaccineimpact.orderlyweb.errors.InvalidOperationError
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.permissions.Role
import org.vaccineimpact.orderlyweb.permissionFromPostData
import org.vaccineimpact.orderlyweb.permissionFromRouteParams
import org.vaccineimpact.orderlyweb.viewmodels.RoleViewModel

class RoleController(
        context: ActionContext,
        private val roleRepo: RoleRepository,
        private val authRepo: AuthorizationRepository
) : Controller(context)
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
        val name = context.postData<String>()["name"] ?: throw MissingParameterError("name")
        authRepo.createUserGroup(name)
        return okayResponse()
    }

    fun deleteRole(): String {
        val roleId = roleId()
        if (roleId == RoleRepository.ADMIN_ROLE)
        {
            throw InvalidOperationError("You cannot delete the ${RoleRepository.ADMIN_ROLE} role.")
        }

        val roleNames = getAllRoleNames()
        if (!roleNames.contains(roleId))
        {
            throw UnknownObjectError(roleId, "role")
        }

        authRepo.deleteUserGroup(roleId)
        return okayResponse()
    }

    fun addPermission(): String
    {
        val roleId = roleId()

        if (roleId == RoleRepository.ADMIN_ROLE)
        {
            throw InvalidOperationError("You cannot add permissions to the ${RoleRepository.ADMIN_ROLE} role.")
        }

        val permission = context.permissionFromPostData()
        authRepo.ensureUserGroupHasPermission(roleId, permission)

        return okayResponse()
    }

    fun removePermission(): String
    {
        val roleId = roleId()

        if (roleId == RoleRepository.ADMIN_ROLE)
        {
            throw InvalidOperationError("You cannot remove permissions from the ${RoleRepository.ADMIN_ROLE} role.")
        }

        val permission = context.permissionFromRouteParams()
        authRepo.ensureUserGroupDoesNotHavePermission(roleId, permission)

        return okayResponse()
    }

    fun addUser(): String
    {
        val roleId = roleId()
        val email = context.postData<String>()["email"] ?: throw MissingParameterError("action")
        authRepo.ensureGroupHasMember(roleId, email)
        return okayResponse()
    }

    fun removeUser(): String
    {
        val roleId = roleId()
        val email = context.params(":email")

        if (roleId == RoleRepository.ADMIN_ROLE && context.userProfile?.id == email)
        {
            throw InvalidOperationError("You cannot remove yourself from the ${RoleRepository.ADMIN_ROLE} role.")
        }

        authRepo.ensureGroupDoesNotHaveMember(roleId, email)
        return okayResponse()
    }

    private fun roleId(): String = context.params(":role-id")

    private fun List<Role>.toSortedViewModels(): List<RoleViewModel>
    {
        return this.map { RoleViewModel.build(it) }
                .sortedBy{ it.name }
    }
}
