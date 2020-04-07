package org.vaccineimpact.orderlyweb.models

data class ReportVersionDetails(val basicReportVersion: BasicReportVersion,
                                val artefacts: List<Artefact>,
                                val resources: List<FileInfo>,
                                val dataInfo: List<DataInfo>,
                                val parameterValues: Map<String, String>): Version by basicReportVersion


