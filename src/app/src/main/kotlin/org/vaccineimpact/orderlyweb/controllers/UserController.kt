package org.vaccineimpact.orderlyweb.controllers

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.models.AuthenticationResponse
import org.vaccineimpact.orderlyweb.security.WebTokenHelper

class UserController(context: ActionContext,
                     private val tokenHelper: WebTokenHelper,
                     private val serializer: Serializer)
    : Controller(context)
{
    constructor(context: ActionContext) : this(context, WebTokenHelper.instance, Serializer.instance)

    fun githubAuth(): AuthenticationResponse
    {
        return AuthenticationResponse(
                accessToken = tokenHelper.issuer.generateBearerToken(context.userProfile.username),
                expiresIn = tokenHelper.issuer.tokenLifeSpan.seconds)
    }

}