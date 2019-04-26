package org.vaccineimpact.orderlyweb.viewmodels

import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig

open class AppViewModel(val loggedIn: Boolean, val user: String?)
{
    constructor(userProfile: CommonProfile?) : this(userProfile != null, userProfile?.id)
    constructor(context: ActionContext) : this(context.userProfile)

    open val appName = AppConfig()["app.name"]
    val appEmail = AppConfig()["app.email"]
    open val authProvider = AuthenticationConfig.getConfiguredProvider().toString().toLowerCase()
}
