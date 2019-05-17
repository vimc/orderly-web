package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext

class FailedLoginViewModel(context: ActionContext) :
        AppViewModel by DefaultViewModel(context, IndexViewModel.breadcrumb, Breadcrumb("Login failed", null))
