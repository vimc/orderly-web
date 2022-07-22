package org.vaccineimpact.orderlyweb.viewmodels

import org.apache.commons.lang3.time.DurationFormatUtils
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import kotlin.math.roundToLong

data class ReportVersionPageViewModel(
    @Serialise("reportJson") val report: ReportVersionWithDescLatestElapsed,
    val focalArtefactUrl: String?,
    val isRunner: Boolean,
    val artefacts: List<ArtefactViewModel>,
    val dataLinks: List<InputDataViewModel>,
    val resources: List<DownloadableFileViewModel>,
    val zipFile: DownloadableFileViewModel,
    val versions: List<VersionPickerViewModel>,
    val changelog: List<ChangelogViewModel>,
    val parameterValues: String?,
    val instances: Map<String, String>,
    val startTimeString: String,
    val elapsedString: String,
    val appViewModel: AppViewModel
) : AppViewModel by appViewModel
{
    companion object
    {
        fun build(
            report: ReportVersionWithArtefactsDataDescParamsResources,
            versions: List<String>,
            changelog: List<Changelog>,
            context: ActionContext
        ): ReportVersionPageViewModel
        {
            val fileViewModelBuilder = ReportFileViewModelBuilder(report.name, report.id)

            val artefactViewModels = report.artefacts.map {
                buildArtefact(fileViewModelBuilder, it)
            }

            val focalArtefactUrl = getFocalArtefactUrl(fileViewModelBuilder, report.artefacts)

            val dataViewModels = report.dataInfo.map {
                InputDataViewModel(
                        it.name,
                        fileViewModelBuilder.buildDataFileViewModel(it.name, "csv", it.csvSize),
                        fileViewModelBuilder.buildDataFileViewModel(it.name, "rds", it.rdsSize))
            }

            val resourceViewModels = report.resources.map {
                fileViewModelBuilder
                        .buildResourceFileViewModel(it)
            }

            val zipFile = fileViewModelBuilder
                    .buildZipFileViewModel()

            val isRunner = context.hasPermission(ReifiedPermission("reports.run", Scope.Global()))

            val displayName = report.displayName ?: report.name

            val breadcrumb = Breadcrumb("${report.name} (${report.id})",
                    "${AppConfig()["app.url"]}/report/${report.name}/${report.id}/")

            val changelogViewModel = changelog.sortedByDescending { it.reportVersion }
                    .groupBy { it.reportVersion }.map {
                        ChangelogViewModel.build(it.key, it.value)
                    }

            val parameterValues = if (report.parameterValues.keys.count() > 0)
            {
                report.parameterValues.keys.joinToString(", ") { "$it=${report.parameterValues[it]}" }
            }
            else
            {
                null
            }

            val date = getDateStringFromVersionId(report.id)
            val startTimeString = getFriendlyDateTime(date)

            @Suppress("MagicNumber")
            val elapsedMillis = (report.basicReportVersion.elapsed * 1000).roundToLong()
            val elapsedString = DurationFormatUtils.formatDurationWords(elapsedMillis, true, true)

            return ReportVersionPageViewModel(
                    report.basicReportVersion.copy(displayName = displayName),
                    focalArtefactUrl,
                    isRunner,
                    artefactViewModels,
                    dataViewModels,
                    resourceViewModels,
                    zipFile,
                    versions.sortedByDescending { it }.map { buildVersionPickerViewModel(report.name, report.id, it) },
                    changelogViewModel,
                    parameterValues,
                    report.instances,
                    startTimeString,
                    elapsedString,
                    DefaultViewModel(context, IndexViewModel.breadcrumb, breadcrumb))
        }

        private fun getFocalArtefactUrl(builder: ReportFileViewModelBuilder, artefacts: List<Artefact>): String?
        {
            //reproducing existing reportle behaviour - show the first artefact inline if it is possible
            val focalArtefactFile = if (artefacts.any() && artefacts[0].files.any() &&
                    canRenderInBrowser(artefacts[0].files[0].name))
            {
                artefacts[0].files[0]
            }
            else
            {
                null
            }

            return if (focalArtefactFile == null)
            {
                null
            }
            else
            {
                builder
                        .inline()
                        .buildArtefactFileViewModel(focalArtefactFile)
                        .url
            }
        }

        private fun buildArtefact(fileBuilder: ReportFileViewModelBuilder, artefact: Artefact): ArtefactViewModel
        {
            val inlineFile = getInlineFigureFile(artefact.files)
            val inline = if (inlineFile == null)
            {
                null
            }
            else
            {
                fileBuilder
                        .inline()
                        .buildArtefactFileViewModel(inlineFile)
                        .url
            }

            val files = artefact.files.map {
                fileBuilder.buildArtefactFileViewModel(it)
            }

            return ArtefactViewModel(artefact, files, inline)
        }

        private fun getInlineFigureFile(files: List<FileInfo>): FileInfo?
        {
            //reproducing existing reportle behaviour - show the first file inline if it is an image
            return if (files.count() > 0 && isImage(files[0].name))
            {
                files[0]
            }
            else
            {
                null
            }
        }

        private fun buildVersionPickerViewModel(reportName: String, currentVersion: String, id: String):
                VersionPickerViewModel
        {
            val date = getDateStringFromVersionId(id)
            return VersionPickerViewModel("${AppConfig()["app.url"]}/report/$reportName/$id", getFriendlyDateTime(date),
                    selected = id == currentVersion)
        }
    }
}

data class VersionPickerViewModel(val url: String, val date: String, val selected: Boolean)

data class ArtefactViewModel(
    val artefact: Artefact,
    val files: List<DownloadableFileViewModel>,
    val inlineArtefactFigure: String?
)

data class InputDataViewModel(
    val key: String,
    val csv: DownloadableFileViewModel,
    val rds: DownloadableFileViewModel
)

data class DownloadableFileViewModel(val name: String, val url: String, val size: Long?)
{
    val formattedSize =
            if (size != null)
            {
                byteCountToDisplaySize(size)
            }
            else
            {
                null
            }
}

data class ChangelogViewModel(val date: String, val version: String, val entries: List<ChangelogItemViewModel>)
{
    companion object
    {
        fun build(id: String, changelog: List<Changelog>): ChangelogViewModel
        {
            val date = getDateStringFromVersionId(id)

            val entries = changelog.map {
                ChangelogItemViewModel.build(it)
            }
            return ChangelogViewModel(getFriendlyDateTime(date), id, entries)
        }
    }
}

data class ChangelogItemViewModel(val label: String, val value: String, val cssClass: String)
{
    companion object
    {
        fun build(changelog: Changelog): ChangelogItemViewModel
        {
            val cssClass = if (changelog.public)
            {
                "public"
            }
            else
            {
                "internal"
            }
            return ChangelogItemViewModel(changelog.label, changelog.value, cssClass)
        }
    }
}
