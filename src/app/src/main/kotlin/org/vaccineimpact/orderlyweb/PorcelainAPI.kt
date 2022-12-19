package org.vaccineimpact.orderlyweb

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.vaccineimpact.orderlyweb.errors.PorcelainError

interface PorcelainAPI
{
    @Throws(PorcelainError::class)
    fun post(
            url: String,
            context: ActionContext,
            rawRequest: Boolean = false,
            transformResponse: Boolean = true
    ): PorcelainResponse

    @Throws(PorcelainError::class)
    fun post(
            url: String,
            json: String,
            queryParams: Map<String, String?>
    ): PorcelainResponse

    @Throws(PorcelainError::class)
    fun get(url: String, context: ActionContext): PorcelainResponse

    @Throws(PorcelainError::class)
    fun get(url: String, queryParams: Map<String, String>): PorcelainResponse

    @Throws(PorcelainError::class)
    fun delete(url: String, context: ActionContext): PorcelainResponse

    fun throwOnError(): PorcelainAPI
}

class PorcelainResponse(val bytes: ByteArray, val statusCode: Int)
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

open class PorcelainAPIServer(
        private val instanceName: String,
        private val urlBase: String,
        private val client: OkHttpClient = OkHttpClient()
) : PorcelainAPI
{
    protected var throwOnError = false

    private val standardHeaders = mapOf("Accept" to ContentTypes.json)

    override fun throwOnError(): PorcelainAPI
    {
        return apply {
            throwOnError = true
        }
    }

    override fun get(url: String, context: ActionContext): PorcelainResponse
    {
        val request = Request.Builder()
                .url(buildFullUrl(url, context.queryString()))
                .headers(standardHeaders.toHeaders())
                .build()

        client.newCall(request).execute().use {
            if (!it.isSuccessful && throwOnError)
            {
                throw PorcelainError(url, it.code, instanceName)
            }
            return transformResponse(it.code, it.body!!.string())
        }
    }

    override fun get(url: String, queryParams: Map<String, String>): PorcelainResponse
    {
        val buildUrl = urlBase.toHttpUrl().newBuilder()
                .addPathSegments(url.trimStart('/'))
                .apply {
                    queryParams.forEach { (key, value) ->
                        addQueryParameter(key, value)
                    }
                }
        val request = Request.Builder()
                .url(buildUrl.toString())
                .headers(standardHeaders.toHeaders())
                .build()

        client.newCall(request).execute().use {
            if (!it.isSuccessful && throwOnError)
            {
                throw PorcelainError(url, it.code, instanceName)
            }
            return transformResponse(it.code, it.body!!.string())
        }
    }

    override fun post(
            url: String,
            context: ActionContext,
            rawRequest: Boolean,
            transformResponse: Boolean
    ): PorcelainResponse
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
        return post(buildFullUrl(url, context.queryString()).toHttpUrl(), body, transformResponse)
    }

    override fun post(url: String, json: String, queryParams: Map<String, String?>): PorcelainResponse =
            post(
                    urlBase.toHttpUrl().newBuilder()
                            .addPathSegments(url.trimStart('/'))
                            .apply {
                                queryParams.forEach { (key, value) ->
                                    addQueryParameter(key, value)
                                }
                            }.build(),
                    json.toRequestBody(ContentTypes.json.toMediaType())
            )

    private fun post(
            url: HttpUrl,
            body: RequestBody,
            transformResponse: Boolean = true
    ): PorcelainResponse
    {
        val request = Request.Builder()
                .url(url)
                .headers((if (transformResponse) standardHeaders else emptyMap()).toHeaders())
                .post(body)
                .build()
        client.newCall(request).execute().use {
            if (!it.isSuccessful && throwOnError)
            {
                throw PorcelainError(url.toString(), it.code, instanceName)
            }
            return if (transformResponse)
            {
                transformResponse(it.code, it.body!!.string())
            }
            else
            {
                PorcelainResponse(it.body!!.bytes(), it.code)
            }
        }
    }

    override fun delete(url: String, context: ActionContext): PorcelainResponse
    {
        val request = Request.Builder()
                .url(buildFullUrl(url, context.queryString()))
                .headers(standardHeaders.toHeaders())
                .delete()
                .build()
        client.newCall(request).execute().use {
            if (!it.isSuccessful && throwOnError)
            {
                throw PorcelainError(url, it.code, instanceName)
            }
            return transformResponse(it.code, it.body!!.string())
        }
    }

    private fun transformResponse(code: Int, text: String): PorcelainResponse
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

        return PorcelainResponse(json.toString(), code)
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
