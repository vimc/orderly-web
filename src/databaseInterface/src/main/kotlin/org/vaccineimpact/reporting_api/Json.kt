package org.vaccineimpact.reporting_api

import com.google.gson.JsonElement

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
}