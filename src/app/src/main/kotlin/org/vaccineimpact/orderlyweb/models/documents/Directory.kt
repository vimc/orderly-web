package org.vaccineimpact.orderlyweb.models.documents

data class Directory(val path: String, val directoryName: String, val show: Boolean, val displayName: String?,
                     val parentPath: String?)