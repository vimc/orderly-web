package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.errors.MissingRequiredPermissionError
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel

class IndexController : OrderlyDataController
{
    constructor(actionContext: ActionContext,
                orderly: OrderlyClient) : super(actionContext, orderly)

    constructor(actionContext: ActionContext) : super(actionContext)

    @Template("index.ftl")
    fun index(): IndexViewModel
    {
        val reports = orderly.getAllReportVersions()
                .filter { canReadReport(it.name) }

        val pinnedReports = orderly.getGlobalPinnedReports()
                .filter { canReadReport(it.name) }

        return IndexViewModel.build(reports, pinnedReports, context)
    }
}