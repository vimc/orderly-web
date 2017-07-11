package org.vaccineimpact.reporting_api

import com.github.salomonbrys.kotson.jsonSerializer
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import org.vaccineimpact.api.models.*
import org.vaccineimpact.api.models.ResultStatus

open class Serializer
{
    companion object
    {
        val instance = Serializer()
    }

    private val toStringSerializer = jsonSerializer<Any> { JsonPrimitive(it.src.toString()) }

    val gson: Gson

    init
    {
        gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setFieldNamingStrategy { convertFieldName(it.name) }
            .serializeNulls()
            .registerTypeAdapter<java.time.LocalDate>(toStringSerializer)
            .registerTypeAdapter<java.time.Instant>(toStringSerializer)
            .create()
    }

    open fun toResult(data: Any?): String = toJson(org.vaccineimpact.api.models.Result(org.vaccineimpact.api.models.ResultStatus.SUCCESS, data, emptyList()))
    open fun toJson(result: org.vaccineimpact.api.models.Result): String = gson.toJson(result)

    fun convertFieldName(name: String): String
    {
        val builder = StringBuilder()
        for (char in name)
        {
            if (char.isUpperCase())
            {
                builder.append("_" + char.toLowerCase())
            }
            else
            {
                builder.append(char)
            }
        }
        return builder.toString().trim('_')
    }

}