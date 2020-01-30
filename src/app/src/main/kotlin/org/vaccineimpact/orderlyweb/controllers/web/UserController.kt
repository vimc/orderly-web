package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.*
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.permissionFromPostData
import org.vaccineimpact.orderlyweb.permissionFromRouteParams
import org.vaccineimpact.orderlyweb.viewmodels.UserViewModel

class UserController(context: ActionContext,
                     private val userRepo: UserRepository,
                     private val authRepo: AuthorizationRepository,
                     private val roleRepo: RoleRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context,
            OrderlyUserRepository(),
            OrderlyAuthorizationRepository(),
            OrderlyRoleRepository())

    fun getScopedReportReaders(): List<UserViewModel>
    {
        val report = report()
        val users = userRepo.getScopedReportReaderUsers(report)
        return users.mapToUserViewModels()
    }

    fun getGlobalReportReaders(): List<UserViewModel>
    {
        val users = userRepo.getGlobalReportReaderUsers()
        return users.mapToUserViewModels()
    }

    fun getAllUsers(): List<UserViewModel>
    {
        val users = userRepo.getAllUsers()
        val roles = roleRepo.getAllRoles()

        return users.map { user ->
            val rolesForUser = roles.filter {
                it.members.contains(user)
            }
            UserViewModel.build(user, authRepo.getDirectPermissionsForUser(user.email), rolesForUser)
        }.sortedBy { it.displayName.toLowerCase() }
    }

    private fun List<User>.mapToUserViewModels(): List<UserViewModel>
    {
        return this.map { UserViewModel.build(it) }
                .sortedBy { it.displayName.toLowerCase() }
    }

    fun getUserEmails(): List<String>
    {
        return userRepo.getUserEmails()
    }


    fun addPermission(): String
    {
        val userId = userId()
        val permission = context.permissionFromPostData()
        authRepo.ensureUserGroupHasPermission(userId, permission)

        return okayResponse()
    }

    fun removePermission(): String
    {
        val userId = userId()
        val permission = context.permissionFromRouteParams()
        authRepo.ensureUserGroupDoesNotHavePermission(userId, permission)

        return okayResponse()
    }

    private fun userId(): String = context.params(":user-id")
    private fun report(): String = context.params(":report")
}