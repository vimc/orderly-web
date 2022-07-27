package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig

data class AdminViewModel(val appViewModel: AppViewModel, val canAllowGuest: Boolean) : AppViewModel by appViewModel
{
    constructor(context: ActionContext, canAllowGuest: Boolean) : this(
            DefaultViewModel(context, IndexViewModel.breadcrumb, breadcrumb),
            canAllowGuest
    )

    companion object
    {
        val breadcrumb = Breadcrumb("Manage access", "${AppConfig()["app.url"]}/manage-access")
    }
}
