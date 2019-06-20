package org.vaccineimpact.orderlyweb.viewmodels

import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.MissingConfigurationKey
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig

data class Breadcrumb(val name: String, val url: String?)

data class DefaultViewModel(override val loggedIn: Boolean,
                       override val user: String?,
                       override val breadcrumbs: List<Breadcrumb>,
                       private val appConfig: Config = AppConfig()) : AppViewModel
{
    constructor(userProfile: CommonProfile?, breadcrumbs: List<Breadcrumb>) :
            this(userProfile != null, userProfile?.id, breadcrumbs)

    constructor(context: ActionContext, vararg breadcrumbs: Breadcrumb) :
            this(context.userProfile, breadcrumbs.toList())

    override val appName = AppConfig()["app.name"]
    override val appUrl = AppConfig()["app.url"]
    override val appEmail = AppConfig()["app.email"]
    override val authProvider = AuthenticationConfig().getConfiguredProvider().toString()
    override val logo = appConfig["app.logo"]
    override val montaguApiUrl =
            try
            {
                appConfig["montagu.client_api_url"]

            }
            catch (e: MissingConfigurationKey)
            {
                appConfig["montagu.api_url"]
            }
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
    val montaguApiUrl: String
    val appUrl: String
}

