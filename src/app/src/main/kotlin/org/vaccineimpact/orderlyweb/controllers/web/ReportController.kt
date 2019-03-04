package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.ReportVersion
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails

class ReportController(context: ActionContext,
                     private val orderly: OrderlyClient) : Controller(context) {

    constructor(context: ActionContext): this(context, Orderly())

    @Template("reports.ftl")
    fun getAll(): ReportsViewModel {
        return ReportsViewModel(orderly.getAllReportVersions())
    }

    @Template("report.ftl")
    fun get(): ReportVersionViewModel {
        return ReportVersionViewModel(orderly.getDetailsByNameAndVersion(context.params(":name"), context.params(":version")))
    }

    data class ReportVersionViewModel(val report: ReportVersionDetails)

    data class ReportsViewModel(val reports: List<ReportVersion>)
}