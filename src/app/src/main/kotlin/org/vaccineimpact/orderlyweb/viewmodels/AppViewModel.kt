package org.vaccineimpact.orderlyweb.viewmodels

import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebAuthenticationConfig

data class Breadcrumb(val name: String, val url: String?)

data class DefaultViewModel(override val loggedIn: Boolean,
                            override val user: String?,
                            override val isReviewer: Boolean,
                            override val isAdmin: Boolean,
                            override val isAnon: Boolean,
                            override val breadcrumbs: List<Breadcrumb>,
                            private val appConfig: Config = AppConfig()) : AppViewModel
{
    constructor(userProfile: CommonProfile?, isReviewer: Boolean, isAdmin: Boolean, isAnon: Boolean, breadcrumbs: List<Breadcrumb>, appConfig: Config) :
            this(userProfile != null, userProfile?.id, isReviewer, isAdmin, isAnon, breadcrumbs, appConfig)

    constructor(context: ActionContext, vararg breadcrumbs: Breadcrumb, appConfig: Config = AppConfig()):
            this(context.userProfile,
                    context.hasPermission(ReifiedPermission("reports.review", Scope.Global())),
                    context.hasPermission(ReifiedPermission("users.manage", Scope.Global())),
                    context.userProfile?.id == "anon",
                    breadcrumbs.toList(), appConfig)

    override val appName = appConfig["app.name"]
    override val appUrl = appConfig["app.url"]
    override val appEmail = appConfig["app.email"]
    override val logo = appConfig["app.logo"]
    override val montaguUrl = appConfig["montagu.url"]

    override val fineGrainedAuth = appConfig.authorizationEnabled

    override val authProvider = OrderlyWebAuthenticationConfig().getConfiguredProvider().toString()

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

    val isReviewer: Boolean
    val isAdmin: Boolean
    val isAnon: Boolean

    val fineGrainedAuth: Boolean
}

