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
                        i += 1
                        listOf(ReportRowViewModel(parentId, 0, 0, it.key, null, null, null, null, null)) +
                                it.value.map { v -> i += 1; mapVersion(v, i, parentId) }
                    }

            return IndexViewModel(context, reportRows)
        }

        private val formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy, HH:mm")

        private fun mapVersion(reportVersion: ReportVersion, index: Int, parent: Int): ReportRowViewModel
        {
            val dateString = formatter.format(reportVersion.date.atZone(ZoneId.systemDefault()))
            return ReportRowViewModel(index,
                    1,
                    parent,
                    reportVersion.name,
                    reportVersion.id,
                    dateString,
                    reportVersion.published,
                    reportVersion.author,
                    reportVersion.requester)
        }
    }
}

//{"DT_RowId": "2","level": 1,"key": "2","parent": 1,"name": "Nelenil Adam","value": 5


data class ReportRowViewModel(val key: Int,
                              val level: Int,
                              val parent: Int,
                              val name: String,
                              val id: String?,
                              val date: String?,
                              val published: Boolean?,
                              val author: String?,
                              val requester: String?)