package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig

class WebloginViewModel(context: ActionContext, val requestedUrl: String) :
        AppViewModel by DefaultViewModel(context, IndexViewModel.breadcrumb, Breadcrumb("Login", "${AppConfig()["app.url"]}/weblogin"))