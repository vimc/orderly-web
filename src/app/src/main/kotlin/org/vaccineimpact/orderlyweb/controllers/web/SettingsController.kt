package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.repositories.*

class SettingsController(context: ActionContext,
                         private val repo: SettingsRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context,
            OrderlySettingsRepository())

    fun getAuthAllowGuest(): Boolean
    {
        return repo.getAuthAllowGuest()
    }

    fun setAuthAllowGuest()
    {
        val value = context.getRequestBody().toBoolean()
        repo.setAuthAllowGuest(value)
    }
}