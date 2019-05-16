package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.models.Report

open class IndexViewModel(context: ActionContext,
                          open val reports: List<Report>) : AppViewModel(context, this.breadcrumb)
{
    companion object
    {
        val breadcrumb = Breadcrumb("Main menu", "/")
    }
}
