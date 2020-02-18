package org.vaccineimpact.orderlyweb.models

import java.time.Instant

data class ReportVersionDetails(val name: String,
                                val displayName: String?,
                                val id: String,
                                val published: Boolean,
                                val date: Instant,
                                val author: String,
                                val requester: String,
                                val description: String?,
                                val artefacts: List<Artefact>,
                                val resources: List<FileInfo>,
                                val dataInfo: List<DataInfo>)

