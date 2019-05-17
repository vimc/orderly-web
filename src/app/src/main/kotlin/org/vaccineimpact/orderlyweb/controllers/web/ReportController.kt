package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.viewmodels.ReportVersionPageViewModel
import org.vaccineimpact.orderlyweb.viewmodels.ReportVersionViewModelBuilder

class ReportController(context: ActionContext,
                       val orderly: OrderlyClient,
                       private val viewModelBuilder: ReportVersionViewModelBuilder = ReportVersionViewModelBuilder()) : Controller(context)
{
    constructor(context: ActionContext) :
            this(context, Orderly(context.hasPermission(ReifiedPermission("reports.review", Scope.Global()))), ReportVersionViewModelBuilder())

    @Template("report-page.ftl")
    fun getByNameAndVersion(): ReportVersionPageViewModel
    {
        val reportName = context.params(":name")
        val version = context.params(":version")
        val reportDetails = orderly.getDetailsByNameAndVersion(reportName, version)
        return viewModelBuilder.build(reportDetails, context)
    }
}