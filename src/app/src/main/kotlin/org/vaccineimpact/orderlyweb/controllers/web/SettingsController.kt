package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.repositories.*
import org.vaccineimpact.orderlyweb.errors.InvalidOperationError
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebAuthenticationConfig

class SettingsController(context: ActionContext,
                         private val repo: SettingsRepository,
                         private val authConfig: AuthenticationConfig = OrderlyWebAuthenticationConfig())
    : Controller(context)
{
    constructor(context: ActionContext) : this(context,
            OrderlySettingsRepository())

    fun getAuthAllowGuest(): Boolean
    {
        return authConfig.allowGuestUser
    }

    fun setAuthAllowGuest(): String
    {
        if (!authConfig.canAllowGuestUser) {
            throw InvalidOperationError("Cannot set auth-allow-guest with current application configuration")
        }

        val value = context.getRequestBody().toBoolean()
        repo.setAuthAllowGuest(value)

        return okayResponse()
    }
}
