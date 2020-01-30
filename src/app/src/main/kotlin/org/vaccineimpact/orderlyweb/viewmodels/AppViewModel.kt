package org.vaccineimpact.orderlyweb.viewmodels

import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig

data class Breadcrumb(val name: String, val url: String?)

data class DefaultViewModel(override val loggedIn: Boolean,
                            override val user: String?,
                            override val isAdmin: Boolean,
                            override val breadcrumbs: List<Breadcrumb>,
                            private val appConfig: Config = AppConfig()) : AppViewModel
{
    constructor(userProfile: CommonProfile?, isAdmin: Boolean, breadcrumbs: List<Breadcrumb>) :
            this(userProfile != null, userProfile?.id, isAdmin, breadcrumbs)

    constructor(context: ActionContext, vararg breadcrumbs: Breadcrumb) :
            this(context.userProfile, context.hasPermission(ReifiedPermission("reports.review", Scope.Global())), breadcrumbs.toList())

    override val appName = appConfig["app.name"]
    override val appUrl = appConfig["app.url"]
    override val appEmail = appConfig["app.email"]
    override val logo = appConfig["app.logo"]
    override val montaguUrl = appConfig["montagu.url"]

    override val authProvider = AuthenticationConfig().getConfiguredProvider().toString()

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
    val montaguUrl: String
    val appUrl: String

    val isAdmin: Boolean
}

