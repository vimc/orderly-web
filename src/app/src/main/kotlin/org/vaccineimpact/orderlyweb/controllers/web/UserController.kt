package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.permissionFromPostData
import org.vaccineimpact.orderlyweb.permissionFromRouteParams
import org.vaccineimpact.orderlyweb.viewmodels.UserViewModel

class UserController(context: ActionContext,
                     private val userRepo: UserRepository,
                     private val authRepo: AuthorizationRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, OrderlyUserRepository(), OrderlyAuthorizationRepository())

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
        return users.map {
            UserViewModel.build(it, authRepo.getDirectPermissionsForUser(it.email))
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