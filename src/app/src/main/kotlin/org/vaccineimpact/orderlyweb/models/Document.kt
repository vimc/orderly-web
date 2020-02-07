package org.vaccineimpact.orderlyweb.models

data class Document(val displayName: String,
                    val path: String,
                    val file: Boolean,
                    val show: Boolean,
                    val children: List<Document>)
