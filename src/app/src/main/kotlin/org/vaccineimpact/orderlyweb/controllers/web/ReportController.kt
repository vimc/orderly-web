package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebTagRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.TagRepository
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.models.ReportVersionTags
import org.vaccineimpact.orderlyweb.viewmodels.*

class ReportController(context: ActionContext,
                       val orderly: OrderlyClient,
                       private val reportRepository: ReportRepository,
                       private val tagRepository: TagRepository) : Controller(context)
{
    constructor(context: ActionContext)
            : this(context, Orderly(context), OrderlyReportRepository(context), OrderlyWebTagRepository())

    @Template("report-page.ftl")
    fun getByNameAndVersion(): ReportVersionPageViewModel
    {
        val reportName = context.params(":name")
        val version = context.params(":version")
        val reportDetails = orderly.getDetailsByNameAndVersion(reportName, version)
        val versions = reportRepository.getReportsByName(reportName)
        val changelog = orderly.getChangelogByNameAndVersion(reportName, version)
        return ReportVersionPageViewModel.build(reportDetails, versions, changelog, context)
    }

    @Template("run-report-page.ftl")
    fun getRunReport(): RunReportViewModel
    {
        return RunReportViewModel(context)
    }

    fun tagVersion(): String
    {
        val reportName = context.params(":name")
        val versionId = context.params(":version")
        reportRepository.getReportVersion(reportName, versionId)
        val reportTags = context.postData<List<String>>("report_tags")
        val versionTags = context.postData<List<String>>("version_tags")
        tagRepository.updateTags(reportName, versionId, ReportVersionTags(versionTags, reportTags, listOf()))
        return okayResponse()
    }

    fun setGlobalPinnedReports(): String
    {
        val reports = context.postData<List<String>>("reports")

        reports.forEach{
            if (!reportRepository.reportExists(it))
            {
                throw BadRequest("Report '$it' does not exist")
            }
        }
        if (reports.distinct().count() < reports.count())
        {
            throw BadRequest("Cannot include the same pinned report twice")
        }

        reportRepository.setGlobalPinnedReports(reports)

        return okayResponse()
    }
}