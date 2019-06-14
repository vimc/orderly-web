package org.vaccineimpact.orderlyweb.viewmodels

import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig

data class Breadcrumb(val name: String, val url: String?)

data class DefaultViewModel(override val loggedIn: Boolean,
                       override val user: String?,
                       override val breadcrumbs: List<Breadcrumb>) : AppViewModel
{
    constructor(userProfile: CommonProfile?, breadcrumbs: List<Breadcrumb>) :
            this(userProfile != null, userProfile?.id, breadcrumbs)

    constructor(context: ActionContext, vararg breadcrumbs: Breadcrumb) :
            this(context.userProfile, breadcrumbs.toList())

    override val appName = AppConfig()["app.name"]
    override val appUrl = AppConfig()["app.url"]
    override val appEmail = AppConfig()["app.email"]
    override val authProvider = AuthenticationConfig().getConfiguredProvider().toString()
    override val logo = AppConfig()["app.logo"]

    init
    {
        if (!breadcrumbs.any())
        {
            throw Exception("All ViewModel classes must have at least one breadcrumb")
        }
    }
}

interface AppViewModel
{
    val loggedIn: Boolean
    val user: String?
    val breadcrumbs: List<Breadcrumb>
    val appName: String
    val appEmail: String
    val authProvider: String
    val logo: String
    val appUrl: String
}

