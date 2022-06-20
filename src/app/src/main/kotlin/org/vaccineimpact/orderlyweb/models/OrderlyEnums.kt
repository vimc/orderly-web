package org.vaccineimpact.orderlyweb.models

enum class FilePurpose
{
    SOURCE,
    SCRIPT,
    README,
    RESOURCE,
    ORDERLY_YML,
    GLOBAL;

    override fun toString(): String {
        return this.name.lowercase()
    }
}

enum class ArtefactFormat
{
    STATICGRAPH,
    INTERACTIVEGRAPH,
    DATA,
    REPORT,
    INTERACTIVEHTML;

    override fun toString(): String {
        return this.name.lowercase()
    }
}
