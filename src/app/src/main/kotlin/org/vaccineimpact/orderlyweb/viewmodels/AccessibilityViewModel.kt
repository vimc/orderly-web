package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig

data class AccessibilityViewModel(val appViewModel: AppViewModel, val allowGuestUser: Boolean) :
        AppViewModel by appViewModel
{
    constructor(context: ActionContext, allowGuestUser: Boolean) :
            this(DefaultViewModel(
                    context,
                    IndexViewModel.breadcrumb,
                    AccessibilityViewModel.breadcrumb),
                 allowGuestUser)

    companion object
    {
        val breadcrumb = Breadcrumb("Accessibility", "${AppConfig()["app.url"]}/accessibility")
    }
}
