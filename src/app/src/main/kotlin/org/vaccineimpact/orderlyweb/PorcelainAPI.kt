package org.vaccineimpact.orderlyweb

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.vaccineimpact.orderlyweb.errors.PorcelainError

interface PorcelainAPI
{
    @Throws(PorcelainError::class)
    fun post(
            url: String,
            context: ActionContext,
            rawRequest: Boolean = false,
            accept: String = ContentTypes.json
    ): PorcelainResponse

    @Throws(PorcelainError::class)
    fun post(
            url: String,
            json: String,
            queryParams: Map<String, String?>,
            accept: String = ContentTypes.json
    ): PorcelainResponse

    @Throws(PorcelainError::class)
    fun get(url: String, context: ActionContext, accept: String = ContentTypes.json): PorcelainResponse

    @Throws(PorcelainError::class)
    fun get(
            url: String, queryParams: Map<String, String>,
            accept: String = ContentTypes.json
    ): PorcelainResponse

    @Throws(PorcelainError::class)
    fun delete(
            url: String, context: ActionContext,
            accept: String = ContentTypes.json
    ): PorcelainResponse

    fun throwOnError(): PorcelainAPI
}

class PorcelainResponse(val bytes: ByteArray, val statusCode: Int, val headers: Headers)
{
    constructor(text: String, statusCode: Int, headers: Headers) : this(text.toByteArray(), statusCode, headers)

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

open class PorcelainAPIClient(
        private val instanceName: String,
        private val urlBase: String,
        private val client: OkHttpClient = OkHttpClient()
) : PorcelainAPI
{
    protected var throwOnError = false

    override fun throwOnError(): PorcelainAPI
    {
        return apply {
            throwOnError = true
        }
    }

    override fun get(url: String, context: ActionContext, accept: String): PorcelainResponse
    {
        val request = Request.Builder()
                .url(buildFullUrl(url, context.queryString()))
                .headers(mapOf("Accept" to accept).toHeaders())
                .build()

        client.newCall(request).execute().use {
            if (!it.isSuccessful && throwOnError)
            {
                throw PorcelainError(url, it.code, instanceName)
            }
            return PorcelainResponse(it.body!!.bytes(), it.code, it.headers)
        }
    }

    override fun get(url: String, queryParams: Map<String, String>, accept: String): PorcelainResponse
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
                .headers(mapOf("Accept" to accept).toHeaders())
                .build()

        client.newCall(request).execute().use {
            if (!it.isSuccessful && throwOnError)
            {
                throw PorcelainError(url, it.code, instanceName)
            }
            return PorcelainResponse(it.body!!.bytes(), it.code, it.headers)
        }
    }

    override fun post(
            url: String,
            context: ActionContext,
            rawRequest: Boolean,
            accept: String
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
        return post(buildFullUrl(url, context.queryString()).toHttpUrl(), body, accept)
    }

    override fun post(
            url: String,
            json: String,
            queryParams: Map<String, String?>,
            accept: String
    ): PorcelainResponse =
            post(
                    urlBase.toHttpUrl().newBuilder()
                            .addPathSegments(url.trimStart('/'))
                            .apply {
                                queryParams.forEach { (key, value) ->
                                    addQueryParameter(key, value)
                                }
                            }.build(),
                    json.toRequestBody(ContentTypes.json.toMediaType()),
                    accept
            )

    private fun post(
            url: HttpUrl,
            body: RequestBody,
            accept: String
    ): PorcelainResponse
    {
        val request = Request.Builder()
                .url(url)
                .headers(mapOf("Accept" to accept).toHeaders())
                .post(body)
                .build()
        client.newCall(request).execute().use {
            if (!it.isSuccessful && throwOnError)
            {
                throw PorcelainError(url.toString(), it.code, instanceName)
            }
            return PorcelainResponse(it.body!!.bytes(), it.code, it.headers)
        }
    }

    override fun delete(url: String, context: ActionContext, accept: String): PorcelainResponse
    {
        val request = Request.Builder()
                .url(buildFullUrl(url, context.queryString()))
                .headers(mapOf("Accept" to accept).toHeaders())
                .delete()
                .build()
        client.newCall(request).execute().use {
            if (!it.isSuccessful && throwOnError)
            {
                throw PorcelainError(url, it.code, instanceName)
            }
            return PorcelainResponse(it.body!!.bytes(), it.code, it.headers)
        }
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
