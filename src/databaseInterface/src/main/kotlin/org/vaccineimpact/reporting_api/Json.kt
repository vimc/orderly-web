package org.vaccineimpact.reporting_api

import com.google.gson.JsonElement
import com.google.gson.JsonParser

object Json
{
    fun parseScalarOrArray(element: JsonElement): ArrayList<String>
    {
        val result = arrayListOf<String>()

        if (element.isJsonPrimitive)
        {
            result.add(element.asString)
        }

        else if (element.isJsonArray)
        {
            for (subElement in element.asJsonArray)
            {
                result.add(subElement.asString)
            }
        }

        return result
    }

    fun parseSimpleMap(input: String): MutableMap<String, String>
    {
        val simpleMap = mutableMapOf<String, String>()

        val obj = JsonParser()
                .parse(input)
                .asJsonObject

        obj.entrySet().map{
            simpleMap.put(it.key, it.value.asString)
        }

        return simpleMap
    }
}