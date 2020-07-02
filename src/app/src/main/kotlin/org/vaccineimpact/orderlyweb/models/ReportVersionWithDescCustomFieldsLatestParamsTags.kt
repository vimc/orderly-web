package org.vaccineimpact.orderlyweb.models

import java.beans.ConstructorProperties
import java.time.Instant

data class ReportVersionWithDescCustomFieldsLatestParamsTags(@Transient val basicReportVersion: ReportVersionWithDescLatest,
                                                             val customFields: Map<String, String?>,
                                                             val parameterValues: Map<String, String>,
                                                             val tags: List<String>): ReportVersion
{
    override val date: Instant = basicReportVersion.date
    val description: String? = basicReportVersion.description
    override val displayName: String? = basicReportVersion.displayName
    override val id: String = basicReportVersion.id
    override val name: String = basicReportVersion.name
    override val published: Boolean = basicReportVersion.published
    val latestVersion: String = basicReportVersion.latestVersion
}
