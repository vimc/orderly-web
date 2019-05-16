package org.vaccineimpact.orderlyweb.viewmodels

import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig

class Breadcrumb(val name: String, val url: String?)

open class AppViewModel(open val loggedIn: Boolean,
                        open val user: String?,
                        open val breadcrumbs: List<Breadcrumb>)
{
    constructor(userProfile: CommonProfile?, breadcrumbs: List<Breadcrumb>) :
            this(userProfile != null, userProfile?.id, breadcrumbs)

    constructor(context: ActionContext, vararg breadcrumbs: Breadcrumb) :
            this(context.userProfile, breadcrumbs.toList())

    open val appName = AppConfig()["app.name"]
    open val appEmail = AppConfig()["app.email"]
    open val authProvider = AuthenticationConfig().getConfiguredProvider().toString().toLowerCase()
}
