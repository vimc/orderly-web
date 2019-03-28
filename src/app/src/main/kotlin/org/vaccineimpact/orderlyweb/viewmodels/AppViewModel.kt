package org.vaccineimpact.orderlyweb.viewmodels

import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig

open class AppViewModel(val loggedIn: Boolean, val user: String?)
{
    constructor(userProfile: CommonProfile?) : this(userProfile != null, userProfile?.id)
    constructor(context: ActionContext) : this(context.userProfile)

    val appName = AppConfig()["app.name"]
    val appEmail = AppConfig()["app.email"]
}
