package org.vaccineimpact.orderlyweb.controllers

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.TokenStore
import org.vaccineimpact.orderlyweb.security.deflated
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.security.InternalUser
import org.vaccineimpact.orderlyweb.security.OnetimeTokenStore
import org.vaccineimpact.orderlyweb.security.WebTokenHelper

class OnetimeTokenController(context: ActionContext,
                             val tokenStore: OnetimeTokenStore) : Controller(context)
{
    constructor(context: ActionContext) : this(context, TokenStore.instance)

    fun get(): String
    {
        val url = context.queryParams("url")
                ?: throw MissingParameterError("url")

        val profile = context.userProfile

        val username = profile.id
        val permissions = profile.getAttribute("permissions").toString()
        val roles = profile.getAttribute("roles").toString()

        val user = InternalUser(username, roles, permissions)
        val issuer = WebTokenHelper.instance.issuer
        val token = issuer.generateOnetimeActionToken(user, url)

        tokenStore.storeToken(token)

        return token.deflated()
    }
}
