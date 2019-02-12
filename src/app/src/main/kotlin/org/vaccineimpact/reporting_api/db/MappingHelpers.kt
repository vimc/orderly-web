package org.vaccineimpact.reporting_api.db

import org.vaccineimpact.reporting_api.errors.UnknownEnumValue

inline fun <reified T : Enum<T>> parseEnum(name: String): T
{
    return enumValues<T>()
            .firstOrNull { name.equals(it.name, ignoreCase = true) }
            ?: throw UnknownEnumValue(name, T::class.simpleName ?: "[unknown]")
}
