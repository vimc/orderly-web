package org.vaccineimpact.orderlyweb.models

enum class FilePurpose
{
    SOURCE,
    SCRIPT,
    RESOURCE,
    ORDERLY_YML,
    GLOBAL;

    override fun toString(): String {
        return this.name.toLowerCase()
    }
}
