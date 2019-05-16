package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.viewmodels.AppViewModel
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel

class HomeController: OrderlyDataController
{
    constructor(actionContext: ActionContext,
                orderly: OrderlyClient): super(actionContext, orderly)

    constructor(actionContext: ActionContext): super(actionContext)

    @Template("index.ftl")
    fun index(): IndexViewModel
    {
        return IndexViewModel(context, orderly.getAllReports())
    }
}