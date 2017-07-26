package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.db.TokenStore
import org.vaccineimpact.reporting_api.errors.MissingParameterError
import org.vaccineimpact.reporting_api.security.MontaguUser
import org.vaccineimpact.reporting_api.security.USER_OBJECT
import org.vaccineimpact.reporting_api.security.WebTokenHelper

class OnetimeTokenController(val tokenStore: TokenStore = TokenStore()) : Controller
{
    fun get(context: ActionContext): String
    {
        val url = context.queryParams("url")
                ?: throw MissingParameterError("url")

        val token = WebTokenHelper.oneTimeTokenHelper.issuer
                .generateOneTimeActionToken(context.userProfile.getAttribute(USER_OBJECT) as MontaguUser,
                        url)
        tokenStore.storeToken(token)
        return token
    }
}
