package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.db.TokenStore
import org.vaccineimpact.reporting_api.security.MontaguUser
import org.vaccineimpact.reporting_api.security.USER_OBJECT
import org.vaccineimpact.reporting_api.security.WebTokenHelper

class OnetimeTokenController(val tokenStore: TokenStore = TokenStore()) : Controller {

    fun get(context: ActionContext): String {
        val token = WebTokenHelper.oneTimeTokenHelper.issuer
                .generateOneTimeActionToken(context.userProfile.getAttribute(USER_OBJECT) as MontaguUser)
        tokenStore.storeToken(token)
        return token
    }
}
