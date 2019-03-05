package org.vaccineimpact.orderlyweb.models

enum class ArtefactFormat
{
    STATICGRAPH,
    INTERACTIVEGRAPH,
    DATA,
    REPORT,
    INTERACTIVEHTML;

    override fun toString(): String {
        return this.name.toLowerCase()
    }
}
