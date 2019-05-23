package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.models.ReportVersion
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
            var i = 0

            val reportRows = reports.groupBy { it.name }
                    .flatMap {
                        val parentId = i + 1
                        val latestVersion = it.value[0].latestVersion
                        i += 1
                        listOf(ReportRowViewModel(parentId, 0, it.key, latestVersion, it.value.count(), null, null, null)) +
                                it.value.map { v -> i += 1; mapVersion(v, i, parentId) }
                    }

            return IndexViewModel(context, reportRows)
        }

        private val formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy, HH:mm")

        private fun mapVersion(reportVersion: ReportVersion, index: Int, parent: Int): ReportRowViewModel
        {
            return ReportRowViewModel(index,
                    parent,
                    reportVersion.name,
                    reportVersion.id,
                    null,
                    reportVersion.published,
                    reportVersion.author,
                    reportVersion.requester)
        }
    }
}

data class ReportRowViewModel(val ttKey: Int,
                              val ttParent: Int,
                              val name: String,
                              val id: String,
                              val numVersions: Int?,
                              val published: Boolean?,
                              val author: String?,
                              val requester: String?)