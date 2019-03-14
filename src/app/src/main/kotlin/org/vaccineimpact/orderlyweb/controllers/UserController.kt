package org.vaccineimpact.orderlyweb.controllers

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.errors.FailedLoginError
import org.vaccineimpact.orderlyweb.security.WebTokenHelper

class UserController(context: ActionContext, private val tokenHelper: WebTokenHelper)
    : Controller(context)
{
    constructor(context: ActionContext) : this(context, WebTokenHelper.instance)

    private val requiredBasicAuthContentType = "application/x-www-form-urlencoded"

    fun basicAuth(): Boolean
    {
        if (context.contentType() != requiredBasicAuthContentType)
        {
            throw FailedLoginError("Content-Type must be '$requiredBasicAuthContentType'")
        }

        if (context.queryParams("grant_type") != "client_credentials")
        {
            throw FailedLoginError("Expected grant_type to be client_credentials")
        }

        return true
    }

}