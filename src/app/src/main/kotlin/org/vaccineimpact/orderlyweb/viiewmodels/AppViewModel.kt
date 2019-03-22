package org.vaccineimpact.orderlyweb.viiewmodels

import org.vaccineimpact.orderlyweb.db.AppConfig

abstract class AppViewModel {
    val appName = AppConfig()["app.name"]
    val appEmail = AppConfig()["app.email"]
}

data class PageNotFound(val nothing: String) : AppViewModel()