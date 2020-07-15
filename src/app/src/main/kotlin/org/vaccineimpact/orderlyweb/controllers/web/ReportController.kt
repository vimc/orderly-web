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
import org.vaccineimpact.orderlyweb.models.RunReportMetadata
import org.vaccineimpact.orderlyweb.viewmodels.ReportVersionPageViewModel
import org.vaccineimpact.orderlyweb.viewmodels.ReportWithDraftsViewModel
import org.vaccineimpact.orderlyweb.viewmodels.RunReportViewModel

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
        //TODO: Orderly server does not yet support fetching this metadata, so we hardcode dummy data for now
        val runReportMetadata = RunReportMetadata(true, true,
                listOf("support", "annex"), listOf("internal", "published"))

        //TODO: as above, need to get this from orderly server when endpoint is available
        //TODO: Don't attempt get get git branches if metadata.git_supported is false
        val gitBranches = listOf("master", "dev_branch")

        return RunReportViewModel(context, runReportMetadata, gitBranches)
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

    fun getReportDrafts()
            : List<ReportWithDraftsViewModel>
    {
        val reports = reportRepository.getReportsWithPublishStatus()
        val drafts = reportRepository.getDrafts()
        return reports.map { report ->
            ReportWithDraftsViewModel.build(report, drafts.filter { it.name == report.name })
        }.filter { it.dateGroups.any() }

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

    fun publishReports() {
        val ids = context.postData<List<String>>("ids")
        reportRepository.publish(ids)
    }
}
