package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext

class WebloginViewModel(context: ActionContext, val requestedUrl: String) :
        DefaultViewModel(context, Breadcrumb("Login", "/"))