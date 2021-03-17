package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
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
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.viewmodels.PublishReportsViewModel
import org.vaccineimpact.orderlyweb.viewmodels.ReportVersionPageViewModel
import org.vaccineimpact.orderlyweb.viewmodels.ReportWithDraftsViewModel
import org.vaccineimpact.orderlyweb.viewmodels.RunReportViewModel

class ReportController(
    context: ActionContext,
    val orderly: OrderlyClient,
    private val orderlyServerAPI: OrderlyServerAPI,
    private val reportRepository: ReportRepository,
    private val tagRepository: TagRepository
) : Controller(context)
{
    constructor(context: ActionContext) : this(
            context,
            Orderly(context),
            OrderlyServer(AppConfig()),
            OrderlyReportRepository(context),
            OrderlyWebTagRepository()
    )

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
        val reportName = context.queryParams("report-name")
        val metadata = orderlyServerAPI
                .throwOnError()
                .get("/run-metadata", context, true)
                .data(RunReportMetadata::class.java)

        val gitBranches = if (metadata.gitSupported)
        {
            val branchResponse = orderlyServerAPI
                    .throwOnError()
                    .get("/git/branches", context, true)

            branchResponse.listData(GitBranch::class.java)
                    .map { it.name }
        }
        else
        {
            listOf()
        }

        return RunReportViewModel(context, metadata, gitBranches, reportName)
    }

    fun getRunnableReports(): List<ReportWithDate>
    {
        val reports = orderlyServerAPI.get("/reports/source", context).listData(String::class.java)
        val versionedReports = reportRepository.getLatestReportVersions(reports)
        return reports.map { name -> ReportWithDate(name, versionedReports.find { it.name == name }?.date) }
    }

    fun getReportParameters(): List<Parameter>
    {
        val name = context.params(":name")
        return orderlyServerAPI
                .get("/reports/$name/parameters", context)
                .listData(Parameter::class.java)
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
    fun getPublishReports(): PublishReportsViewModel
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
