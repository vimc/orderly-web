package org.vaccineimpact.orderlyweb.db

import org.vaccineimpact.orderlyweb.errors.UnknownEnumValue

inline fun <reified T : Enum<T>> parseEnum(name: String): T
{
    return enumValues<T>()
            .firstOrNull { name.equals(it.name, ignoreCase = true) }
            ?: throw UnknownEnumValue(name, T::class.simpleName ?: "[unknown]")
}
