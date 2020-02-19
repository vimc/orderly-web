package org.vaccineimpact.orderlyweb

import com.github.salomonbrys.kotson.addProperty
import com.github.salomonbrys.kotson.jsonSerializer
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.vaccineimpact.orderlyweb.models.Result
import org.vaccineimpact.orderlyweb.models.ResultStatus
import org.vaccineimpact.orderlyweb.viewmodels.ReportRowViewModel
import kotlin.reflect.full.memberProperties

open class Serializer
{
    companion object
    {
        val instance = Serializer()
    }

    private val toStringSerializer = jsonSerializer<Any> { JsonPrimitive(it.src.toString()) }
    private val enumSerializer = jsonSerializer<Any> { JsonPrimitive(serializeEnum(it.src)) }

    private val reportRowViewModelSerializer = jsonSerializer<ReportRowViewModel> {

        val obj = JsonObject()

        for(prop in ReportRowViewModel::class.memberProperties)
        {
            if (prop.name != "customFields")
            {
                obj.addProperty(convertFieldName(prop.name), prop.get(it.src), it.context)
            }
        }

        for(key in it.src.customFields.keys)
        {
            obj.addProperty(key, it.src.customFields[key])
        }

        obj
    }

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
                .registerTypeAdapter<ResultStatus>(enumSerializer)
                .registerTypeAdapter<ReportRowViewModel>(reportRowViewModelSerializer)
                .create()
    }

    open fun toResult(data: Any?): String = toJson(Result(ResultStatus.SUCCESS, data, emptyList()))

    open fun toJson(result: org.vaccineimpact.orderlyweb.models.Result): String = gson.toJson(result)

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


    fun serializeEnum(value: Any) = value.toString().toLowerCase().replace('_', '-')

}