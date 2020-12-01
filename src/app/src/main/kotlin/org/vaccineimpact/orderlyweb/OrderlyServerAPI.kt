package org.vaccineimpact.orderlyweb

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.OrderlyServerError

interface OrderlyServerAPI
{
    @Throws(OrderlyServerError::class)
    fun post(url: String, context: ActionContext): OrderlyServerResponse

    @Throws(OrderlyServerError::class)
    fun get(url: String, context: ActionContext): OrderlyServerResponse

    @Throws(OrderlyServerError::class)
    fun delete(url: String, context: ActionContext): OrderlyServerResponse

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
                    private val client: OkHttpClient = OkHttpClient(),
                    private val throwOnError: Boolean = false) : OrderlyServerAPI
{
    private val headers = mapOf(
            "Accept" to ContentTypes.json,
            "Accept-Encoding" to "gzip"
    )

    private val urlBase: String = config["orderly.server"]

    override fun throwOnError(): OrderlyServerAPI
    {
        return OrderlyServer(config, client, true)
    }

    override fun get(url: String, context: ActionContext): OrderlyServerResponse
    {
        val request = Request.Builder()
                .url(buildFullUrl(url, context.queryString()))
                .headers(headers.toHeaders())
                .build()
        val response = client.newCall(request).execute()
        return transformResponse(url, response)
    }

    override fun post(url: String, context: ActionContext): OrderlyServerResponse
    {
        val json = context.postData<String>()
        val body = if (json.any())
        {
            Gson().toJson(json).toRequestBody(ContentTypes.json.toMediaType())
        }
        else
        {
            "".toRequestBody()
        }
        val request = Request.Builder()
                .url(buildFullUrl(url, context.queryString()))
                .headers(headers.toHeaders())
                .post(body)
                .build()
        val response = client.newCall(request).execute()
        return transformResponse(url, response)
    }

    override fun delete(url: String, context: ActionContext): OrderlyServerResponse
    {
        val request = Request.Builder()
                .url(buildFullUrl(url, context.queryString()))
                .headers(headers.toHeaders())
                .delete()
                .build()
        val response = client.newCall(request).execute()
        return transformResponse(url, response)
    }

    private fun transformResponse(url: String, rawResponse: Response): OrderlyServerResponse
    {
        val errorsKey = "errors"
        val messageKey = "message"
        val detailKey = "detail"

        val json = JSONObject(rawResponse.body!!.string())

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

        if (!rawResponse.isSuccessful && throwOnError)
        {
            throw OrderlyServerError(url, rawResponse.code)
        }
        return OrderlyServerResponse(json.toString(), rawResponse.code)
    }

    private fun buildFullUrl(url: String, queryString: String?) = "${urlBase}${url}?${queryString ?: ""}"
}
