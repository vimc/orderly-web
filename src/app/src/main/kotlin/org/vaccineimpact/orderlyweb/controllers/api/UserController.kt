package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.models.AuthenticationResponse
import org.vaccineimpact.orderlyweb.security.WebTokenHelper

class UserController(context: ActionContext,
                     private val tokenHelper: WebTokenHelper)
    : Controller(context)
{
    constructor(context: ActionContext) : this(context, WebTokenHelper.instance)

    fun auth(): AuthenticationResponse
    {
        return AuthenticationResponse(
                accessToken = tokenHelper.issuer.generateBearerToken(context.userProfile.id),
                expiresIn = tokenHelper.issuer.tokenLifeSpan.seconds)
    }

}