package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.repositories.UserRepository
import org.vaccineimpact.orderlyweb.models.AuthenticationResponse
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.security.WebTokenHelper

class UserController(context: ActionContext,
                     private val tokenHelper: WebTokenHelper,
                     private val userRepo: UserRepository)
    : Controller(context)
{
    constructor(context: ActionContext) : this(context, WebTokenHelper.instance, OrderlyUserRepository())

    fun auth(): AuthenticationResponse
    {
        @Suppress("UnsafeCallOnNullableType")
        return AuthenticationResponse(
                accessToken = tokenHelper.issuer.generateBearerToken(context.userProfile!!.id),
                expiresIn = tokenHelper.issuer.tokenLifeSpan.seconds)
    }

    fun addUser(): String
    {
        val source = UserSource.valueOf(context.postData("source"))
        userRepo.addUser(context.postData("email"), context.postData("username"),
                            context.postData("displayName"), source)
        return okayResponse()
    }
}