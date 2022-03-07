package org.vaccineimpact.orderlyweb.models

import org.vaccineimpact.orderlyweb.db.parseEnum

data class Artefact(var format: String, val description: String, val files: List<FileInfo>)
{
    init
    {
        this.format = parseEnum<ArtefactFormat>(format).toString()
    }

    constructor(format: ArtefactFormat, description: String, files: List<FileInfo>) : this(
        format.toString(),
        description,
        files
    )
}
