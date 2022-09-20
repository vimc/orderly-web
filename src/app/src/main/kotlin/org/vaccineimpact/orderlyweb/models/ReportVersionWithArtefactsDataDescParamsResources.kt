package org.vaccineimpact.orderlyweb.models

import java.time.Instant

data class ReportVersionWithArtefactsDataDescParamsResources(
         @Transient val basicReportVersion: ReportVersionWithDescLatestElapsed,
         val artefacts: List<Artefact>,
         val resources: List<FileInfo>,
         val dataInfo: List<DataInfo>,
         val parameterValues: Map<String, String>,
         val instances: Map<String, String>
) : ReportVersion
{
    override val date: Instant = basicReportVersion.date
    val description: String? = basicReportVersion.description
    override val displayName: String? = basicReportVersion.displayName
    override val id: String = basicReportVersion.id
    override val name: String = basicReportVersion.name
    override val published: Boolean = basicReportVersion.published
}
