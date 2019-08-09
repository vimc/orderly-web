package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.viewmodels.UserViewModel

class UserController(context: ActionContext,
                     private val userRepo: UserRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, OrderlyUserRepository())

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

    fun List<User>.mapToUserViewModels(): List<UserViewModel>
    {
        return this.map { UserViewModel.build(it) }
                .sortedBy { it.displayName.toLowerCase() }
    }

    fun getUserEmails(): List<String>
    {
        return userRepo.getUserEmails()
    }

    private fun report(): String = context.params(":report")
}