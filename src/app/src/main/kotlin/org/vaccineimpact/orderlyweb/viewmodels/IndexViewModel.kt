package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.models.Report

class IndexViewModel(context: ActionContext,
                     val reports: List<Report>) : AppViewModel by DefaultViewModel(context, this.breadcrumb)
{
    companion object
    {
        val breadcrumb = Breadcrumb("Main menu", "/")
    }
}
