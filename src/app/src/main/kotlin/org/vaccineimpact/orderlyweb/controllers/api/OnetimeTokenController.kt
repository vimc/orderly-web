package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.TokenStore
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.db.OnetimeTokenStore
import org.vaccineimpact.orderlyweb.security.WebTokenHelper

class OnetimeTokenController(context: ActionContext,
                             private val tokenStore: OnetimeTokenStore) : Controller(context)
{
    constructor(context: ActionContext) : this(context, TokenStore.instance)

    fun get(): String
    {
        val url = context.queryParams("url")
                ?: throw MissingParameterError("url")

        val profile = context.userProfile

        val emailAddress = profile!!.id

        val issuer = WebTokenHelper.instance.issuer
        val token = issuer.generateOnetimeActionToken(emailAddress, url)

        tokenStore.storeToken(token)

        return token
    }
}
