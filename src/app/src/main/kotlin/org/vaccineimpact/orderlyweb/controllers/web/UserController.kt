package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.viewmodels.ReportReaderViewModel

class UserController(context: ActionContext,
                     private val userRepo: UserRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, OrderlyUserRepository())

    fun getReportReaders(): List<ReportReaderViewModel>
    {
        val report = report()
        val users = userRepo.getReportReaders(report)
        return users.map { ReportReaderViewModel.build(it.key, it.value) }
                .sortedBy { it.displayName.toLowerCase() }
    }

    private fun report(): String = context.params(":report")
}