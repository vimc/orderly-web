package org.vaccineimpact.reporting_api.errors

import org.vaccineimpact.api.models.ErrorInfo

class UnknownObjectError(id: Any, typeName: Any) : MontaguError(404, listOf(
        org.vaccineimpact.api.models.ErrorInfo("unknown-${mangleTypeName(typeName)}", "Unknown ${mangleTypeName(typeName)} with id '$id'")
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
