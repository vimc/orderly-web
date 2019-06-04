package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.viewmodels.ReportVersionPageViewModel

class ReportController: OrderlyDataController
{
    constructor (context: ActionContext, orderly: OrderlyClient) :
            super(context, orderly)

    constructor(context: ActionContext) : super(context)

    @Template("report-page.ftl")
    fun getByNameAndVersion(): ReportVersionPageViewModel
    {
        val reportName = context.params(":name")
        val version = context.params(":version")
        val reportDetails = orderly.getDetailsByNameAndVersion(reportName, version)
        val versions = orderly.getReportsByName(reportName)
        val changelog = orderly.getChangelogByNameAndVersion(reportName, version)
        return ReportVersionPageViewModel.build(reportDetails, versions, changelog, context)

    }
}