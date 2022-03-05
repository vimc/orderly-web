package org.vaccineimpact.orderlyweb.security.providers

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonSyntaxException
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import java.io.IOException

interface MontaguAPIClient
{
    @Throws(MontaguAPIException::class)
    fun getUserDetails(token: String): UserDetails

    data class UserDetails(val email: String, val username: String, val name: String?)

    // The following are identical to orderlyweb.models.Result, orderlyweb.models.ResultStatus,
    // and orderlyweb.models.ErrorInfo but in principle they need not be and should either spec
    // change could diverge, so defining Montagu specific models here
    data class Result(val status: String, val data: Any?, val errors: List<ErrorInfo>)

    data class ErrorInfo(val code: String, val message: String)
    {
        override fun toString(): String = message
    }
}

abstract class OkHttpMontaguAPIClient(appConfig: Config) : MontaguAPIClient
{

    companion object
    {
        fun create(appConfig: Config = AppConfig()): OkHttpMontaguAPIClient
        {
            return if (appConfig.getBool("allow.localhost"))
            {
                LocalOkHttpMontaguApiClient(appConfig)
            }
            else
            {
                RemoteHttpMontaguApiClient(appConfig)
            }
        }
    }

    private val urlBase = appConfig["montagu.api_url"]
    private val serializer = Serializer.instance.gson

    override fun getUserDetails(token: String): MontaguAPIClient.UserDetails
    {
        getHttpResponse("$urlBase/user/", mapOf("Authorization" to "Bearer $token"))
                .use { response ->
                    val result = parseResult(response.body!!.string())

                    if (response.code != 200)
                    {
                        val errors = result.errors.joinToString(",") { it.toString() }
                        throw MontaguAPIException("Response had errors $errors", response.code)
                    }

                    return result.data as MontaguAPIClient.UserDetails
                }
    }

    private fun parseResult(jsonAsString: String): MontaguAPIClient.Result
    {
        return try
        {
            val result = serializer.fromJson<MontaguAPIClient.Result>(jsonAsString)
            if (result.data.toString().isNotEmpty())
            {
                result.copy(data = serializer.fromJson<MontaguAPIClient.UserDetails>(serializer.toJson(result.data)))
            }
            else result
        }
        catch (e: JsonSyntaxException)
        {
            throw MontaguAPIException("Failed to parse text as JSON.\nText was: $jsonAsString\n\n$e", 500)
        }
    }

    private fun getHttpResponse(url: String, headers: Map<String, String>): Response
    {
        val client = getHttpClient()

        val headersBuilder = Headers.Builder()
        headers.forEach { k, v -> headersBuilder.add(k, v)}

        val request = Request.Builder()
                .url(url)
                .headers(headersBuilder.build())
                .build()

        return client.newCall(request).execute()
    }

    protected abstract fun getHttpClient(): OkHttpClient
}

class LocalOkHttpMontaguApiClient(appConfig: Config) : OkHttpMontaguAPIClient(appConfig)
{
    override fun getHttpClient(): OkHttpClient
    {
        return getLocalOkHttpClient()
    }
}

class RemoteHttpMontaguApiClient(appConfig: Config) : OkHttpMontaguAPIClient(appConfig)
{
    override fun getHttpClient(): OkHttpClient
    {
        return OkHttpClient()
    }
}

class MontaguAPIException(override val message: String, val status: Int) : IOException(message)
