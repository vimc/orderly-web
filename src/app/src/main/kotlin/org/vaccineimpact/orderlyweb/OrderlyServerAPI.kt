package org.vaccineimpact.orderlyweb

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.OrderlyServerError

interface OrderlyServerAPI
{
    @Throws(OrderlyServerError::class)
    fun post(
        url: String,
        context: ActionContext,
        rawRequest: Boolean = false,
        transformResponse: Boolean = true
    ): OrderlyServerResponse

    fun post(
        url: String,
        data: String,
        params: Map<String, String?>
    ): OrderlyServerResponse

    @Throws(OrderlyServerError::class)
    fun get(url: String, context: ActionContext): OrderlyServerResponse

    @Throws(OrderlyServerError::class)
    fun delete(url: String, context: ActionContext): OrderlyServerResponse

    fun throwOnError(): OrderlyServerAPI
}

class OrderlyServerResponse(val bytes: ByteArray, val statusCode: Int)
{
    constructor(text: String, statusCode: Int) : this(text.toByteArray(), statusCode)

    val text get() = String(bytes)

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

class OrderlyServer(
    config: Config,
    private val client: OkHttpClient = OkHttpClient()
) : OrderlyServerAPI
{
    private var throwOnError = false

    private val standardHeaders = mapOf("Accept" to ContentTypes.json)

    private val urlBase: String = config["orderly.server"]

    override fun throwOnError(): OrderlyServerAPI
    {
        return apply {
            throwOnError = true
        }
    }

    override fun get(url: String, context: ActionContext): OrderlyServerResponse
    {
        val request = Request.Builder()
            .url(buildFullUrl(url, context.queryString()))
            .headers(standardHeaders.toHeaders())
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful && throwOnError)
        {
            throw OrderlyServerError(url, response.code)
        }
        return transformResponse(response.code, response.body!!.string())
    }

    override fun post(
        url: String,
        context: ActionContext,
        rawRequest: Boolean,
        transformResponse: Boolean
    ): OrderlyServerResponse
    {
        val body = if (rawRequest)
        {
            context.getRequestBodyAsBytes().toRequestBody(ContentTypes.binarydata.toMediaType())
        }
        else
        {
            val json = context.postData<String>()
            if (json.any())
            {
                Gson().toJson(json).toRequestBody(ContentTypes.json.toMediaType())
            }
            else
            {
                "".toRequestBody()
            }
        }
        val headers = if (transformResponse)
        {
            standardHeaders
        }
        else
        {
            emptyMap()
        }
        val request = Request.Builder()
            .url(buildFullUrl(url, context.queryString()))
            .headers(headers.toHeaders())
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful && throwOnError)
        {
            throw OrderlyServerError(url, response.code)
        }
        return if (transformResponse)
        {
            transformResponse(response.code, response.body!!.string())
        }
        else
        {
            OrderlyServerResponse(response.body!!.bytes(), response.code)
        }
    }

    override fun post(url: String, data: String, params: Map<String, String?>): OrderlyServerResponse
    {
        val httpUrl = urlBase.toHttpUrl().newBuilder()
            .addPathSegments(url.trimStart('/'))
            .apply {
                params.forEach { (key, value) ->
                    addQueryParameter(key, value)
                }
            }.build()
        val request = Request.Builder()
            .url(httpUrl)
            .headers(standardHeaders.toHeaders())
            .post(data.toRequestBody(ContentTypes.json.toMediaType()))
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful && throwOnError)
        {
            throw OrderlyServerError(url, response.code)
        }
        return transformResponse(response.code, response.body!!.string())
    }

    override fun delete(url: String, context: ActionContext): OrderlyServerResponse
    {
        val request = Request.Builder()
            .url(buildFullUrl(url, context.queryString()))
            .headers(standardHeaders.toHeaders())
            .delete()
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful && throwOnError)
        {
            throw OrderlyServerError(url, response.code)
        }
        return transformResponse(response.code, response.body!!.string())
    }

    private fun transformResponse(code: Int, text: String): OrderlyServerResponse
    {
        val errorsKey = "errors"
        val messageKey = "message"
        val detailKey = "detail"

        val json = JSONObject(text)

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

        return OrderlyServerResponse(json.toString(), code)
    }

    private fun buildFullUrl(url: String, queryString: String?): String
    {
        val parameters = if (queryString != null)
        {
            "?$queryString"
        }
        else
        {
            ""
        }
        return "$urlBase$url$parameters"
    }
}
