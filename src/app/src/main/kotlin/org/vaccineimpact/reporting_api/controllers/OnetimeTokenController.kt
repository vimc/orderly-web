package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.db.TokenStore
import org.vaccineimpact.reporting_api.errors.MissingParameterError
import org.vaccineimpact.reporting_api.security.MontaguUser
import org.vaccineimpact.reporting_api.security.OnetimeTokenStore
import org.vaccineimpact.reporting_api.security.WebTokenHelper

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

        val user = MontaguUser(username, roles, permissions)
        val issuer = WebTokenHelper.oneTimeTokenHelper.issuer
        val token = issuer.generateOnetimeActionToken(user, url)

        tokenStore.storeToken(token)

        return token
    }
}
