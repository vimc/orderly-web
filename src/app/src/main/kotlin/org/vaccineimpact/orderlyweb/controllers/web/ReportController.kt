package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebTagRepository
import org.vaccineimpact.orderlyweb.db.repositories.TagRepository
import org.vaccineimpact.orderlyweb.viewmodels.ReportVersionPageViewModel

class ReportController(context: ActionContext,
                       val orderly: OrderlyClient,
                       private val tagRepository: TagRepository) : Controller(context)
{
    constructor(context: ActionContext)
            : this(context, Orderly(context), OrderlyWebTagRepository())

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

    fun tagReport(): String
    {
        val reportName = context.params(":name")
        val tag = context.postData("tag")
        tagRepository.tagReport(reportName, tag)
        return okayResponse()
    }

    fun tagVersion(): String
    {
        val reportName = context.params(":name")
        val versionId = context.params(":version")
        orderly.checkVersionExistsForReport(reportName, versionId)
        val tag = context.postData("tag")
        tagRepository.tagVersion(versionId, tag)
        return okayResponse()
    }

    fun deleteReportTag(): String
    {
        val reportName = context.params(":name")
        val tag = context.postData("tag")
        tagRepository.deleteReportTag(reportName, tag)
        return okayResponse()
    }

    fun deleteVersionTag(): String
    {
        val reportName = context.params(":name")
        val versionId = context.params(":version")
        orderly.checkVersionExistsForReport(reportName, versionId)
        val tag = context.postData("tag")
        tagRepository.deleteVersionTag(versionId, tag)
        return okayResponse()
    }
}