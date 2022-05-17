package org.vaccineimpact.orderlyweb.errors

import kotlin.reflect.KClass

class UnknownObjectError(id: Any, typeName: Any) : OrderlyWebError(404, listOf(
        org.vaccineimpact.orderlyweb.models.ErrorInfo("unknown-${mangleTypeName(typeName)}", "Unknown ${mangleTypeName(typeName)} : '$id'")
))
{
    constructor(id: Any, type: KClass<*>)
            : this(id, type.simpleName ?: "unknown-type")

    companion object
    {
        fun mangleTypeName(typeName: Any) = typeName
                .toString()
                .replace(Regex("[A-Z]"), { "-" + it.value.lowercase() })
                .trim('-')
    }
}
