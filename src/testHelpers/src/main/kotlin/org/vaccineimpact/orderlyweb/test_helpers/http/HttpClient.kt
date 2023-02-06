package org.vaccineimpact.orderlyweb.test_helpers.http

import okhttp3.FormBody
import okhttp3.Headers.Companion.toHeaders
import okhttp3.JavaNetCookieJar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.Closeable
import java.net.CookieManager
import java.util.*

object HttpClient
{
    fun get(url: String, headers: Map<String, String>, allowRedirects: Boolean = false): Response
    {
        val client = OkHttpClient.Builder()
            .followRedirects(allowRedirects)
            .apply {
                if (allowRedirects)
                {
                    cookieJar(JavaNetCookieJar(CookieManager()))
                }
            }
            .build()
        val request = Request.Builder()
            .url(url)
            .headers(headers.toHeaders())
            .build()
        OkHttpClient().newCall(request).execute().use {
            return Response(it)
        }
    }

    fun post(url: String, data: Map<String, String>, auth: Authorization): Response
    {
        val request = Request.Builder()
            .url(url)
            .header(auth.header.first, auth.header.second)
            .post(
                FormBody.Builder().apply {
                    data.forEach { (t, u) -> add(t, u) }
                }.build()
            )
            .build()
        OkHttpClient().newCall(request).execute().use {
            return Response(it)
        }
    }

    fun post(url: String, headers: Map<String, String>, json: Map<String, Any>?): Response
    {
        val request = Request.Builder()
            .url(url)
            .headers(headers.toHeaders())
            .post((if (json == null) "" else JSONObject(json).toString()).toRequestBody("application/json".toMediaType()))
            .build()
        OkHttpClient().newCall(request).execute().use {
            return Response(it)
        }
    }

    fun post(url: String) = post(url, emptyMap(), "")
    fun post(url: String, auth: Authorization) = post(url, emptyMap(), auth)
    fun post(url: String, headers: Map<String, String>, data: String?): Response
    {
        val request = Request.Builder()
            .url(url)
            .headers(headers.toHeaders())
            .post((data ?: "").toRequestBody())
            .build()
        OkHttpClient().newCall(request).execute().use {
            return Response(it)
        }
    }

    fun delete(url: String, headers: Map<String, String>): Response
    {
        val request = Request.Builder()
            .url(url)
            .headers(headers.toHeaders())
            .delete()
            .build()
        OkHttpClient().newCall(request).execute().use {
            return Response(it)
        }
    }

    fun options(url: String): Response
    {
        val request = Request.Builder()
            .url(url)
            .method("OPTIONS", null)
            .build()
        OkHttpClient().newCall(request).execute().use {
            return Response(it)
        }
    }
}

interface Authorization
{
    val header: Pair<String, String>
}

data class BasicAuthorization(val user: String, val password: String) : Authorization
{
    override val header: Pair<String, String>
        get()
        {
            val b64 = Base64.getEncoder().encode("${this.user}:${this.password}".toByteArray()).toString(Charsets.UTF_8)
            return "Authorization" to "Basic $b64"
        }
}

class Response(private val okHttpResponse: okhttp3.Response): Closeable
{
    val content: ByteArray by lazy(okHttpResponse.body!!::bytes)
    val headers: Map<String, String>
        get() = okHttpResponse.headers.toMap().mapKeys { it.key.lowercase() }
    val statusCode: Int
        get() = okHttpResponse.code
    val text: String by lazy(okHttpResponse.body!!::string)
    override fun close()
    {
        okHttpResponse.close()
    }
}
