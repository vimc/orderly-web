package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
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
        return AuthenticationResponse(
                accessToken = tokenHelper.issuer.generateBearerToken(context.userProfile!!.id),
                expiresIn = tokenHelper.issuer.tokenLifeSpan.seconds)
    }

    fun addUser()
    {
        val source = UserSource.valueOf(getFromPosted("source");
        userRepo.addUser(getFromPosted("email"), getFromPosted("username"), getFromPosted("displayName"), source);
    }

    private fun getFromPosted(key: String): String
    {
        return context.postData()[key] ?: throw MissingParameterError(key);
    }
}