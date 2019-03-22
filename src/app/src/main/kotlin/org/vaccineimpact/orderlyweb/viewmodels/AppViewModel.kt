package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.db.AppConfig

open class AppViewModel {
    val appName = AppConfig()["app.name"]
    val appEmail = AppConfig()["app.email"]
}
