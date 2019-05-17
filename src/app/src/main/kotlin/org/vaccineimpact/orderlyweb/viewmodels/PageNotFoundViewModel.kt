package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext

class PageNotFoundViewModel(context: ActionContext) :
        AppViewModel by DefaultViewModel(context, IndexViewModel.breadcrumb, Breadcrumb("Page not found", null))