package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.models.Report

data class IndexViewModel(val reports: List<Report>, val appViewModel: AppViewModel) : AppViewModel by appViewModel
{
    constructor(context: ActionContext, reports: List<Report>)
            : this(reports, DefaultViewModel(context, IndexViewModel.breadcrumb))

    companion object
    {
        val breadcrumb = Breadcrumb("Main menu", "/")
    }
}
