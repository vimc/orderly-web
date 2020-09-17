package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.KHttpClient
import org.vaccineimpact.orderlyweb.OrderlyServer
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebTagRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.TagRepository
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.models.ReportVersionTags
import org.vaccineimpact.orderlyweb.models.RunReportMetadata
import org.vaccineimpact.orderlyweb.viewmodels.PublishReportsViewModel
import org.vaccineimpact.orderlyweb.viewmodels.ReportVersionPageViewModel
import org.vaccineimpact.orderlyweb.viewmodels.ReportWithDraftsViewModel
import org.vaccineimpact.orderlyweb.viewmodels.RunReportViewModel

class ReportController(context: ActionContext,
                       val orderly: OrderlyClient,
                       val orderlyServerAPI: OrderlyServerAPI,
                       private val reportRepository: ReportRepository,
                       private val tagRepository: TagRepository) : Controller(context)
{
    constructor(context: ActionContext)
            : this(context,
            Orderly(context),
            OrderlyServer(AppConfig(), KHttpClient()),
            OrderlyReportRepository(context),
            OrderlyWebTagRepository())

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
        //TODO: replace with call to orderly server
        val runReportMetadata = RunReportMetadata(true, true,
                listOf("support", "annex"), listOf("internal", "published"))

        // TODO only fetch branches if metadata supports it
        val branchResponse = orderlyServerAPI.get("/git/branches", context)
        val gitBranches = if (branchResponse.statusCode == 200)
        {
            branchResponse.data<List<Map<String, String>>>()
                    ?.mapNotNull { it["name"] }?: listOf()
        }
        else
        {
            listOf()
        }

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

    @Template("publish-reports.ftl")
    fun getPublishReports()
            : PublishReportsViewModel
    {
        return PublishReportsViewModel(context)
    }

    fun getDrafts(): List<ReportWithDraftsViewModel>
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

        reports.forEach {
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

    fun publishReports(): String
    {
        val ids = context.postData<List<String>>("ids")
        reportRepository.publish(ids)
        return okayResponse()
    }
}
