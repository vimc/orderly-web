package org.vaccineimpact.orderlyweb

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.vaccineimpact.orderlyweb.app_start.OrderlyWeb.Companion.httpClient
import org.vaccineimpact.orderlyweb.errors.PorcelainError
import org.vaccineimpact.orderlyweb.models.ErrorInfo

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

    fun errors(): List<ErrorInfo>?
    {
        return try
        {
            val element = JsonParser().parse(text)
            val errors = element.asJsonObject["errors"]
            return if (errors.isJsonNull)
            {
                null
            }
            else
            {
                Serializer.instance.gson.fromJson(errors)
            }
        }
        catch (e: JsonSyntaxException)
        {
            listOf(ErrorInfo("bad-json", text))
        }
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
        private val client: OkHttpClient = httpClient
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

        return execute(url, request)
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

        return execute(url, request)
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
        return execute(url.toString(), request)
    }

    override fun delete(url: String, context: ActionContext, accept: String): PorcelainResponse
    {
        val request = Request.Builder()
                .url(buildFullUrl(url, context.queryString()))
                .headers(mapOf("Accept" to accept).toHeaders())
                .delete()
                .build()
        return execute(url, request)
    }

    private fun execute(url: String, request: Request): PorcelainResponse
    {
        client.newCall(request).execute().use {
            val response = PorcelainResponse(it.body!!.bytes(), it.code, it.headers)
            if (!it.isSuccessful && throwOnError)
            {
                throw PorcelainError(url, it.code, instanceName, response.errors())
            }
            return response
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
