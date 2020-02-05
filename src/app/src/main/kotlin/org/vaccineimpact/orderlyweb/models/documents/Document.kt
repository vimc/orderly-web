package org.vaccineimpact.orderlyweb.models.documents

data class Document(val id: Int, val filename: String, val show: Boolean, val dirPath: String,
                    val displayName: String?, val description: String?)