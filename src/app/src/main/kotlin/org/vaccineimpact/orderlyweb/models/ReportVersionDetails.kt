package org.vaccineimpact.orderlyweb.models

import java.time.Instant

data class ReportVersionDetails(@Transient val basicReportVersion: BasicReportVersion,
                                val artefacts: List<Artefact>,
                                val resources: List<FileInfo>,
                                val dataInfo: List<DataInfo>,
                                val parameterValues: Map<String, String>) : Version by basicReportVersion
{
    // we have to declare these overrides so that this gets serialised as a flat object
    override val date: Instant = basicReportVersion.date
    override val description: String? = basicReportVersion.description
    override val displayName: String? = basicReportVersion.displayName
    override val id: String = basicReportVersion.id
    override val name: String = basicReportVersion.name
    override val latestVersion: String = basicReportVersion.latestVersion
    override val published: Boolean = basicReportVersion.published
}
