package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller

class SettingsController(context: ActionContext) : Controller(context)
{
    fun getAuthAllowGuest(): Boolean
    {
        return false;
    }

    fun setAuthAllowGuest()
    {}
}