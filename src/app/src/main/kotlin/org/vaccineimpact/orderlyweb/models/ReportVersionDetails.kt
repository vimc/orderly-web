package org.vaccineimpact.orderlyweb.models

import java.time.Instant

data class ReportVersionDetails(@Transient val basicReportVersion: BasicReportVersion,
                                val artefacts: List<Artefact>,
                                val resources: List<FileInfo>,
                                val dataInfo: List<DataInfo>,
                                val parameterValues: Map<String, String>)
{
    val date: Instant = basicReportVersion.date
    val description: String? = basicReportVersion.description
    val displayName: String? = basicReportVersion.displayName
    val id: String = basicReportVersion.id
    val name: String = basicReportVersion.name
    val published: Boolean = basicReportVersion.published
}
