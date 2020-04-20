package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.models.ReportDraft
import org.vaccineimpact.orderlyweb.models.ReportWithPublishStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
            return ReportDraftViewModel(version.id, "${AppConfig()["app.url"]}/${version.name}/${version.id}", changelogs, parameterValues)
        }
    }
}


data class DateGroup(val date: String, val drafts: List<ReportDraftViewModel>)
{
    companion object
    {
        fun build(date: Instant, versions: List<ReportDraft>): DateGroup
        {
            val formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy")
                    .withZone(ZoneId.systemDefault())

            return DateGroup(formatter.format(date), versions
                    .sortedByDescending { it.date }
                    .map { ReportDraftViewModel.build(it) })
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
                    versions.groupBy { v -> v.date.truncatedTo(ChronoUnit.DAYS) }
                            .toSortedMap(reverseOrder())
                            .map { DateGroup.build(it.key, it.value) })
        }
    }
}
