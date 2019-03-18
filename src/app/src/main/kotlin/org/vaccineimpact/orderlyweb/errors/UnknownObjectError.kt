package org.vaccineimpact.orderlyweb.errors

class UnknownObjectError(id: Any, typeName: Any) : OrderlyWebError(404, listOf(
        org.vaccineimpact.orderlyweb.models.ErrorInfo("unknown-${mangleTypeName(typeName)}", "Unknown ${mangleTypeName(typeName)} : '$id'")
))
{
    companion object
    {
        fun mangleTypeName(typeName: Any) = typeName
                .toString()
                .replace(Regex("[A-Z]"), { "-" + it.value.toLowerCase() })
                .trim('-')
    }
}
