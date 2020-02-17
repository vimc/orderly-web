package org.vaccineimpact.orderlyweb.models

data class Artefact(val format: ArtefactFormat, val description: String, val files: List<FileInfo>)
