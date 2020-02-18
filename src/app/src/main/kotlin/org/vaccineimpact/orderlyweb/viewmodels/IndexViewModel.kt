package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.models.ReportVersion
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class IndexViewModel(@Serialise("reportsJson") val reports: List<ReportRowViewModel>,
                          val pinnedReports: List<PinnedReportViewModel>,
                          val customFieldKeys: List<String>,
                          val appViewModel: AppViewModel)
    : AppViewModel by appViewModel
{
    constructor(context: ActionContext,
                reports: List<ReportRowViewModel>,
                pinnedReports: List<PinnedReportViewModel>,
                customFieldKeys: List<String>)
            : this(reports, pinnedReports, customFieldKeys, DefaultViewModel(context, breadcrumb))

    companion object
    {
        val breadcrumb = Breadcrumb("Main menu", AppConfig()["app.url"])

        fun build(reports: List<ReportVersion>,
                  pinnedReports: List<ReportVersion>,
                  context: ActionContext): IndexViewModel
        {
            val customFieldKeys = if (reports.count() > 0)
            {
                reports[0].customFields.keys
            }
            else {
                setOf()
            }

            var currentKey = 0
            val reportRows = reports.groupBy { it.name }.flatMap {
                currentKey += 1
                val parent = ReportRowViewModel.buildParent(currentKey, it.value, customFieldKeys)

                val children = it.value.sortedByDescending { v -> v.date }.map { version ->
                    currentKey += 1
                    ReportRowViewModel.buildVersion(version, currentKey, parent)
                }

                children + parent
            }

            val pinnedReportsViewModels = PinnedReportViewModel.buildList(pinnedReports)

            return IndexViewModel(context, reportRows, pinnedReportsViewModels, customFieldKeys.sorted())
        }
    }
}

private object IndexViewDateFormatter
{
    private val formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy")

    fun format(date: Instant): String
    {
        return formatter.format(LocalDateTime.ofInstant(date, ZoneId.of("UTC")))
    }
}

data class ReportRowViewModel(val ttKey: Int,
                              val ttParent: Int,
                              val name: String,
                              val displayName: String,
                              val id: String,
                              val latestVersion: String,
                              val date: String?,
                              val numVersions: Int,
                              val published: Boolean?,
                              val customFields: Map<String, String?>)
{
    companion object
    {
        fun buildParent(key: Int, versions: List<ReportVersion>, customFieldKeys: Set<String>): ReportRowViewModel
        {
            val latestVersion = versions.sortedByDescending { it.date }.first()
            val numVersions = versions.count()
            val displayName = latestVersion.displayName?: latestVersion.name


            val pairs = customFieldKeys.map{ k -> k to null as String?}.toTypedArray()
            val emptyCustomFields = mutableMapOf(*pairs)

            return ReportRowViewModel(key, 0, latestVersion.name, displayName,
                    latestVersion.id, latestVersion.id, null, numVersions, null, emptyCustomFields)
        }

        fun buildVersion(version: ReportVersion, key: Int, parent: ReportRowViewModel): ReportRowViewModel
        {
            val dateString = IndexViewDateFormatter.format(version.date)

            return ReportRowViewModel(key,
                    parent.ttKey,
                    version.name,
                    parent.displayName,
                    version.id,
                    parent.latestVersion,
                    dateString,
                    parent.numVersions,
                    version.published,
                    version.customFields)

        }
    }
}

data class PinnedReportViewModel(val name: String,
                                 val version: String,
                                 val displayName: String,
                                 val date: String,
                                 val zipFile: DownloadableFileViewModel)
{
    companion object
    {
        fun buildList(versions: List<ReportVersion>): List<PinnedReportViewModel>
        {
            return versions.map{

                val reportFileViewModelBuilder = ReportFileViewModelBuilder(it.name, it.id)
                PinnedReportViewModel(it.name,
                        it.id,
                        it.displayName?:it.name,
                        IndexViewDateFormatter.format(it.date),
                        reportFileViewModelBuilder.buildZipFileViewModel())
            }
        }
    }
}
