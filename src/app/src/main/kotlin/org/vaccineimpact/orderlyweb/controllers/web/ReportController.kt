package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.viewmodels.ReportVersionPageViewModel

class ReportController : OrderlyDataController
{
    private val authRepo: AuthorizationRepository

    constructor (context: ActionContext, orderly: OrderlyClient, authRepo: AuthorizationRepository) :
            super(context, orderly)
    {
        this.authRepo = authRepo
    }

    constructor(context: ActionContext) : super(context)
    {
        this.authRepo = OrderlyAuthorizationRepository()
    }

    @Template("report-page.ftl")
    fun getByNameAndVersion(): ReportVersionPageViewModel
    {
        val reportName = context.params(":name")
        val version = context.params(":version")
        val reportDetails = orderly.getDetailsByNameAndVersion(reportName, version)
        val versions = orderly.getReportsByName(reportName)
        val changelog = orderly.getChangelogByNameAndVersion(reportName, version)
        return ReportVersionPageViewModel.build(reportDetails, versions, changelog, context, authRepo)

    }
}