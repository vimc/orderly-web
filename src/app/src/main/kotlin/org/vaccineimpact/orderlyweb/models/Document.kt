package org.vaccineimpact.orderlyweb.models

data class Document(
        val name: String,
        val displayName: String,
        val path: String,
        val file: Boolean,
        val external: Boolean,
        val children: List<Document>
)
