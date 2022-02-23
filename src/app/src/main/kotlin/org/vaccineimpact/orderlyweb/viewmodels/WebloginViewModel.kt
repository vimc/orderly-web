package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext

class WebloginViewModel(context: ActionContext, val requestedUrl: String) :
        AppViewModel by DefaultViewModel(context, IndexViewModel.breadcrumb, Breadcrumb("Login", "/weblogin"))