package org.vaccineimpact.orderlyweb.models

import org.vaccineimpact.orderlyweb.db.parseEnum

data class Artefact(val format: ArtefactFormat, val description: String, val files: List<FileInfo>)
{
    constructor(format: String, description: String, files: List<FileInfo>) : this(
        parseEnum<ArtefactFormat>(format),
        description,
        files
    )
}

