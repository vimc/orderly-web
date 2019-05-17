package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext

open class FailedLoginViewModel(context: ActionContext) :
        DefaultViewModel(context, IndexViewModel.breadcrumb, Breadcrumb("Login failed", null))
