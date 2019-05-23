package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.models.ReportVersion
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class IndexViewModel(@Serialise("reportsJson") val reports: List<ReportRowViewModel>, val appViewModel: AppViewModel)
    : AppViewModel by appViewModel
{
    constructor(context: ActionContext, reports: List<ReportRowViewModel>)
            : this(reports, DefaultViewModel(context, IndexViewModel.breadcrumb))

    companion object
    {
        val breadcrumb = Breadcrumb("Main menu", "/")

        fun build(reports: List<ReportVersion>, context: ActionContext): IndexViewModel
        {
            var currentKey = 0
            val reportRows = reports.groupBy { it.name }.flatMap {
                currentKey += 1
                val parent = ReportRowViewModel.buildParent(currentKey, it.value)

                val children = it.value.map { version ->
                    currentKey += 1
                    ReportRowViewModel.buildVersion(version, currentKey, parent)
                }

                children + parent
            }

            return IndexViewModel(context, reportRows)
        }
    }
}

data class ReportRowViewModel(val ttKey: Int,
                              val ttParent: Int,
                              val name: String,
                              val id: String?,
                              val latestVersion: String,
                              val date: String?,
                              val numVersions: Int?,
                              val published: Boolean?,
                              val author: String?,
                              val requester: String?)
{
    companion object
    {
        private val formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy, HH:mm")

        fun buildParent(key: Int, versions: List<ReportVersion>): ReportRowViewModel
        {
            val referenceVersion = versions.sortedByDescending { it.date }.first()
            val latestVersion = referenceVersion.latestVersion
            val numVersions = versions.count()
            return ReportRowViewModel(key, 0, referenceVersion.name, null, latestVersion, null, numVersions, null, null, null)
        }

        fun buildVersion(version: ReportVersion, key: Int, parent: ReportRowViewModel): ReportRowViewModel
        {
            val dateString = formatter.format(LocalDateTime.ofInstant(version.date, ZoneId.of("UTC")))

            return ReportRowViewModel(key,
                    parent.ttKey,
                    version.name,
                    version.id,
                    parent.latestVersion,
                    dateString,
                    parent.numVersions,
                    version.published,
                    version.author,
                    version.requester)

        }
    }
}
