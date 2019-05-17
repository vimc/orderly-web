package org.vaccineimpact.orderlyweb.viewmodels

import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig

class Breadcrumb(val name: String, val url: String?)

open class DefaultViewModel(context: ActionContext, vararg breadcrumbs: Breadcrumb):
        AppViewModel(context)
{
    override val breadcrumbs = breadcrumbs.toList()
}

abstract class AppViewModel(open val loggedIn: Boolean,
                            open val user: String?)
{
    constructor(userProfile: CommonProfile?) :
        this(userProfile != null, userProfile?.id)

    constructor(context: ActionContext) :
            this(context.userProfile)

    abstract val breadcrumbs: List<Breadcrumb>

    val appName = AppConfig()["app.name"]
    val appEmail = AppConfig()["app.email"]
    val authProvider = AuthenticationConfig().getConfiguredProvider().toString().toLowerCase()
}

