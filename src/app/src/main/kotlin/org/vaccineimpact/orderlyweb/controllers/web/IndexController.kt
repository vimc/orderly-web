package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyTagRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.TagRepository
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel

class IndexController(actionContext: ActionContext,
                      private val orderly: OrderlyClient,
                      private val reportRepository: ReportRepository,
                      private val tagRepository: TagRepository) : Controller(actionContext)
{
    constructor(context: ActionContext)
            : this(context, Orderly(context), OrderlyReportRepository(context), OrderlyTagRepository())

    @Template("index.ftl")
    fun index(): IndexViewModel
    {
        val reports = orderly.getAllReportVersions()
        val reportNames = reports.map { it.name }.distinct()
        val reportTags = tagRepository.getReportTags(reportNames)
        val allTags = tagRepository.getAllTags()
        val pinnedReports = reportRepository.getGlobalPinnedReports()
        return IndexViewModel.build(reports, reportTags, allTags, pinnedReports, context)
    }

    fun metrics(): String
    {
        return "running 1"
    }
}
