package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.getDateStringFromVersionId
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.models.ReportVersionWithDescCustomFieldsLatestParamsTags
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class IndexViewModel(@Serialise("reportsJson") val reports: List<ReportRowViewModel>,
                          val tags: List<String>,
                          val pinnedReports: List<PinnedReportViewModel>,
                          val customFieldKeys: List<String>,
                          val showProjectDocs: Boolean,
                          val canSetPinnedReports: Boolean,
                          @Serialise("reportDisplayNamesJson") val reportDisplayNames: Map<String, String>?,
                          val appViewModel: AppViewModel,
                          val isRunner: Boolean? = false) : AppViewModel by appViewModel
{
    constructor(context: ActionContext,
                reports: List<ReportRowViewModel>,
                tags: List<String>,
                pinnedReports: List<PinnedReportViewModel>,
                customFieldKeys: List<String>,
                showProjectDocs: Boolean,
                canSetPinnedReports: Boolean,
                reportDisplayNames: Map<String, String>?) : this(reports, tags, pinnedReports, customFieldKeys,
            showProjectDocs, canSetPinnedReports, reportDisplayNames, DefaultViewModel(context, breadcrumb))

    constructor(context: ActionContext,
                reports: List<ReportRowViewModel>,
                tags: List<String>,
                pinnedReports: List<PinnedReportViewModel>,
                customFieldKeys: List<String>,
                showProjectDocs: Boolean,
                canSetPinnedReports: Boolean,
                reportDisplayNames: Map<String, String>?,
                isRunner: Boolean?) : this(reports, tags, pinnedReports, customFieldKeys, showProjectDocs,
            canSetPinnedReports, reportDisplayNames, DefaultViewModel(context, breadcrumb), isRunner)

    companion object
    {
        val breadcrumb = Breadcrumb("Main menu", AppConfig()["app.url"])

        fun build(reports: List<ReportVersionWithDescCustomFieldsLatestParamsTags>,
                  reportTags: Map<String, List<String>>,
                  allTags: List<String>,
                  pinnedReports: List<Report>,
                  context: ActionContext): IndexViewModel
        {
            val emptyCustomFields: Map<String, String?> = if (reports.count() > 0)
            {
                reports[0].customFields.mapValues { null }
            }
            else
            {
                mapOf()
            }

            var currentKey = 0
            val reportRows = reports.groupBy { it.name }.flatMap {
                currentKey += 1

                val parentTags = reportTags[it.key] ?: listOf()

                val parent = ReportRowViewModel.buildParent(currentKey, it.value, emptyCustomFields, parentTags)

                val children = it.value.sortedByDescending { v -> v.date }.map { version ->
                    currentKey += 1
                    ReportRowViewModel.buildVersion(version, currentKey, parent)
                }

                children + parent
            }

            val pinnedReportsViewModels = PinnedReportViewModel.buildList(pinnedReports)
            val showDocs = context.hasPermission(ReifiedPermission("documents.read", Scope.Global()))
            val isRunner = context.hasPermission(ReifiedPermission("reports.run", Scope.Global()))

            val canSetPinnedReports = context.hasPermission(ReifiedPermission("pinned-reports.manage", Scope.Global()))
            val reportDisplayNames = if (canSetPinnedReports)
            {
                val reportsWithPublished = reportRows.filter { it.ttParent != 0 && it.published == true }
                        .map { it.ttParent }.distinct()

                reportRows.filter { it.ttParent == 0 && reportsWithPublished.contains(it.ttKey) }
                        .map { it.name to it.displayName }.toMap()
            }
            else
            {
                null
            }

            return IndexViewModel(context, reportRows, allTags, pinnedReportsViewModels,
                    emptyCustomFields.keys.sorted(), showDocs, canSetPinnedReports, reportDisplayNames, isRunner)
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

    fun format(id: String): String
    {
        return formatter.format(getDateStringFromVersionId(id))
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
                              val customFields: Map<String, String?>,
                              val parameterValues: String?,
                              val tags: List<String>)
{
    companion object
    {
        fun buildParent(key: Int,
                        versions: List<ReportVersionWithDescCustomFieldsLatestParamsTags>,
                        customFields: Map<String, String?>,
                        tags: List<String>): ReportRowViewModel
        {
            val latestVersion = versions.sortedByDescending { it.date }.first()
            val numVersions = versions.count()
            val displayName = latestVersion.displayName ?: latestVersion.name

            return ReportRowViewModel(key, 0, latestVersion.name, displayName,
                    latestVersion.id, latestVersion.id, null, numVersions, null, customFields, null,
                    tags)
        }

        fun buildVersion(version: ReportVersionWithDescCustomFieldsLatestParamsTags, key: Int, parent: ReportRowViewModel): ReportRowViewModel
        {
            val dateString = IndexViewDateFormatter.format(version.date)
            val parameterValues = if (version.parameterValues.keys.count() > 0)
            {
                version.parameterValues.keys.joinToString(", ") { "$it=${version.parameterValues[it]}" }
            }
            else
            {
                null
            }

            return ReportRowViewModel(key,
                    parent.ttKey,
                    version.name,
                    parent.displayName,
                    version.id,
                    parent.latestVersion,
                    dateString,
                    parent.numVersions,
                    version.published,
                    version.customFields,
                    parameterValues,
                    version.tags)
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
        fun buildList(versions: List<Report>): List<PinnedReportViewModel>
        {
            return versions.map {

                val reportFileViewModelBuilder = ReportFileViewModelBuilder(it.name, it.latestVersion)
                PinnedReportViewModel(it.name,
                        it.latestVersion,
                        it.displayName ?: it.name,
                        IndexViewDateFormatter.format(it.latestVersion),
                        reportFileViewModelBuilder.buildZipFileViewModel())
            }
        }
    }
}
