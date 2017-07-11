package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.Orderly
import org.vaccineimpact.reporting_api.OrderlyClient
import org.vaccineimpact.reporting_api.models.OrderlyReport

class ReportController(orderlyClient: OrderlyClient? = null) : Controller
{
    val orderly = orderlyClient?: Orderly()

    fun getAll(context: ActionContext): List<String> {
        return orderly.getAllReports()
    }

    fun getByName(context: ActionContext): List<String> {
        return orderly.getReportsByName(context.params(":name"))
    }

    fun getByNameAndVersion(context: ActionContext): OrderlyReport {
        return orderly.getReportsByNameAndVersion(context.params(":name"), context.params(":version"))
    }
}