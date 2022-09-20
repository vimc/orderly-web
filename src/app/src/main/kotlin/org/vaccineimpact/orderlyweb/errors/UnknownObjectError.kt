package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus
import kotlin.reflect.KClass

class UnknownObjectError(id: Any, typeName: Any) : OrderlyWebError(
    HttpStatus.NOT_FOUND_404,
    listOf(
        org.vaccineimpact.orderlyweb.models.ErrorInfo(
                "unknown-${mangleTypeName(typeName)}",
                "Unknown ${mangleTypeName(typeName)} : '$id'"
        )
    )
)
{
    constructor(id: Any, type: KClass<*>) : this(id, type.simpleName ?: "unknown-type")

    companion object
    {
        fun mangleTypeName(typeName: Any) = typeName
                .toString()
                .replace(Regex("[A-Z]"), { "-" + it.value.lowercase() })
                .trim('-')
    }
}
