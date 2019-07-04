package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
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
        val pinnedReports = orderly.getGlobalPinnedReports()
        return IndexViewModel.build(reports, pinnedReports, context)
    }

    fun metrics(): String
    {
        return "running 1"
    }
}