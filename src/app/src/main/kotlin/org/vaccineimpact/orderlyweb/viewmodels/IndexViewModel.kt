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
            val reportRows = reports.groupBy { it.name }
                    .map {
                        ReportRowViewModel(it.key, null, null, null, null, null,
                                it.value.map(::mapVersion))
                    }

            return IndexViewModel(context, reportRows)
        }

        private val formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy, HH:mm")

        private fun mapVersion(reportVersion: ReportVersion): ReportVersionRowViewModel
        {
            val dateString = formatter.format(reportVersion.date.atZone(ZoneId.systemDefault()))
            return ReportVersionRowViewModel(reportVersion.name,
                    reportVersion.id,
                    dateString,
                    reportVersion.published,
                    reportVersion.author,
                    reportVersion.requester)
        }
    }
}

data class ReportVersionRowViewModel(val name: String,
                                     val id: String,
                                     val date: String,
                                     val published: Boolean,
                                     val author: String,
                                     val requester: String)

data class ReportRowViewModel(val name: String,
                              val id: String?,
                              val date: String?,
                              val published: Boolean?,
                              val author: String?,
                              val requester: String?,
                              val children: List<ReportVersionRowViewModel>)