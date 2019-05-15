package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext

class FailedLoginViewModel(context: ActionContext) : AppViewModel(context, listOf(BreadCrumb("Login failed", null)))
