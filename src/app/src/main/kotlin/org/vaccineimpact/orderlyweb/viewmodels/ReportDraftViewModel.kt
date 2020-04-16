package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.models.ReportDraft
import org.vaccineimpact.orderlyweb.models.ReportWithPublishStatus
import java.time.Instant

data class ReportDraftViewModel(val id: String,
                                val url: String,
                                val changelog: List<ChangelogItemViewModel>,
                                val parameterValues: String?)
{
    companion object
    {
        fun build(version: ReportDraft): ReportDraftViewModel
        {
            val changelogs = version.changelogs.map { ChangelogItemViewModel.build(it) }
            val parameterValues = version.parameterValues.entries.joinToString(",") { "${it.key}=${it.value}" }
            return ReportDraftViewModel(version.id, "${version.name}/${version.id}", changelogs, parameterValues)
        }
    }
}


data class DateGroup(val date: String, val drafts: List<ReportDraftViewModel>)
{
    companion object
    {
        fun build(date: Instant, versions: List<ReportDraft>): DateGroup
        {
            return DateGroup(date.toString(), versions.map { ReportDraftViewModel.build(it) })
        }
    }
}

data class ReportWithDraftsViewModel(val displayName: String, val previouslyPublished: Boolean, val dateGroups: List<DateGroup>)
{
    companion object
    {
        fun build(report: ReportWithPublishStatus, versions: List<ReportDraft>): ReportWithDraftsViewModel
        {
            return ReportWithDraftsViewModel(report.displayName ?: report.name,
                    report.hasBeenPublished,
                    versions.groupBy { v -> v.date }
                            .map { DateGroup.build(it.key, it.value) })
        }
    }
}
