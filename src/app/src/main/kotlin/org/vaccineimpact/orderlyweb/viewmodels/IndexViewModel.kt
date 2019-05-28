package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.models.ReportVersion
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class IndexViewModel(@Serialise("reportsJson") val reports: List<ReportRowViewModel>, val isReviewer: Boolean,
                          val appViewModel: AppViewModel)
    : AppViewModel by appViewModel
{
    constructor(context: ActionContext, reports: List<ReportRowViewModel>, isReviewer: Boolean)
            : this(reports, isReviewer, DefaultViewModel(context, breadcrumb))

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

            return IndexViewModel(context, reportRows,
                    context.hasPermission(ReifiedPermission("reports.review", Scope.Global())))
        }
    }
}

data class ReportRowViewModel(val ttKey: Int,
                              val ttParent: Int,
                              val name: String,
                              val displayName: String,
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
        private val formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy")

        fun buildParent(key: Int, versions: List<ReportVersion>): ReportRowViewModel
        {
            val referenceVersion = versions.sortedByDescending { it.date }.first()
            val latestVersion = referenceVersion.latestVersion
            val numVersions = versions.count()
            val displayName = referenceVersion.displayName?: referenceVersion.name
            return ReportRowViewModel(key, 0, referenceVersion.name, displayName,
                    null, latestVersion, null, numVersions, null, null, null)
        }

        fun buildVersion(version: ReportVersion, key: Int, parent: ReportRowViewModel): ReportRowViewModel
        {
            val dateString = formatter.format(LocalDateTime.ofInstant(version.date, ZoneId.of("UTC")))

            return ReportRowViewModel(key,
                    parent.ttKey,
                    version.name,
                    parent.displayName,
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
