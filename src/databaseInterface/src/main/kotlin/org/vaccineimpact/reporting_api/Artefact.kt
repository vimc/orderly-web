package org.vaccineimpact.reporting_api

import com.google.gson.*

data class Artefact(val type: ArtefactType, val description: String, val files: ArrayList<String>)

enum class ArtefactType
{
    DATA,
    STATICGRAPH,
    INTERACTIVEGRAPH
}

fun Artefact.hasFile(filename: String) : Boolean
{
    return this.files.contains(filename)
}

fun parseArtefacts(artefactString: String): ArrayList<Artefact>
{
    val array = arrayListOf<Artefact>()

    val artefactArray = JsonParser()
            .parse(artefactString)
            .asJsonArray

    for (artefactElement in artefactArray){

        val artefactObject = artefactElement
                .asJsonObject
                .entrySet()
                .first()

        val artefact = parseArtefact(artefactObject.key, artefactObject.value)

        array.add(artefact)
    }

    return array
}

private fun parseArtefact(artefactKey: String, artefactValue: JsonElement): Artefact
{
    val key = ArtefactType.valueOf(artefactKey.toUpperCase())

    val artefactObject = artefactValue.asJsonObject
    val description = artefactObject.get("description").asString

    val files = Json.parseScalarOrArray(artefactObject.get("filename"))

    return Artefact(key, description, files)
}