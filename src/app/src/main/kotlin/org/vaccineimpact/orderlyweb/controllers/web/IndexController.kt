package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel

class IndexController: OrderlyDataController
{
    constructor(actionContext: ActionContext,
                orderly: OrderlyClient): super(actionContext, orderly)

    constructor(actionContext: ActionContext): super(actionContext)

    @Template("index.ftl")
    fun index(): IndexViewModel
    {
        return IndexViewModel.build(orderly.getAllReportVersions(),
                orderly.getGlobalPinnedReports(), context)
    }
}