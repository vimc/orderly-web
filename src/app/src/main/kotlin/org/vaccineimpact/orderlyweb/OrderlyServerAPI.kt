package org.vaccineimpact.orderlyweb

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import khttp.responses.Response
import org.json.JSONArray
import org.json.JSONObject
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.OrderlyServerError

interface OrderlyServerAPI
{
    fun post(url: String, context: ActionContext): OrderlyServerResponse
    fun get(url: String, context: ActionContext): OrderlyServerResponse

    fun throwOnError(): OrderlyServerAPI
}

data class OrderlyServerResponse(val text: String, val statusCode: Int)
{
    fun <T> data(klass: Class<T>): T
    {
        val data = parseJson(text)
        return Serializer.instance.gson.fromJson(data, klass)
    }

    fun <T> listData(klass: Class<T>): List<T>
    {
        val data = parseJson(text)
        val type = TypeToken.getParameterized(List::class.java, klass).type
        return Serializer.instance.gson.fromJson(data, type)
    }

    private fun parseJson(jsonAsString: String): JsonElement
    {
        val element = JsonParser().parse(jsonAsString)
        return element.asJsonObject["data"]
    }
}

class OrderlyServer(private val config: Config,
                    private val httpClient: HttpClient,
                    private val throwOnError: Boolean = false) : OrderlyServerAPI
{
    private val urlBase: String = config["orderly.server"]

    private val standardHeaders = mapOf(
            "Accept" to ContentTypes.json,
            "Accept-Encoding" to "gzip"
    )

    override fun throwOnError(): OrderlyServerAPI
    {
        return OrderlyServer(config, httpClient, true)
    }

    override fun post(url: String, context: ActionContext): OrderlyServerResponse
    {
        val fullUrl = buildFullUrl(url, context.queryString())
        val postData = context.postData<String>()
        val response = httpClient.post(fullUrl, standardHeaders, postData)
        return transformResponse(url, response)
    }

    override fun get(url: String, context: ActionContext): OrderlyServerResponse
    {
        val fullUrl = buildFullUrl(url, context.queryString())
        val response = httpClient.get(fullUrl, standardHeaders)
        return transformResponse(url, response)
    }

    private fun transformResponse(url: String, rawResponse: Response): OrderlyServerResponse
    {
        val errorsKey = "errors"
        val messageKey = "message"
        val detailKey = "detail"

        val json = rawResponse.jsonObject
        val newErrors = JSONArray()
        if (json.has(errorsKey) && json[errorsKey] is JSONArray)
        {
            val errors = json[errorsKey] as JSONArray
            for (error in errors)
            {
                if (error is JSONObject)
                {
                    if (error.has(detailKey))
                    {
                        val message = if (error[detailKey] == JSONObject.NULL)
                        {
                            ""
                        }
                        else
                        {
                            error[detailKey]
                        }
                        error.put(messageKey, message)
                        error.remove(detailKey)
                    }

                    newErrors.put(error)
                }
            }
        }
        json.put("errors", newErrors)

        if (rawResponse.statusCode > 200 && throwOnError)
        {
            throw OrderlyServerError(url, rawResponse.statusCode)
        }
        return OrderlyServerResponse(json.toString(), rawResponse.statusCode)
    }

    private fun buildFullUrl(url: String, queryString: String?): String
    {
        val queryPart = if (queryString != null)
        {
            "?" + queryString
        }
        else
        {
            ""
        }
        return urlBase + url + queryPart
    }
}