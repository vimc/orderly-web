package org.vaccineimpact.orderlyweb.security.providers

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonSyntaxException
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import java.io.IOException
import java.security.cert.X509Certificate
import javax.net.ssl.*


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
                LocalOkHttpMontaguApiClient(appConfig)
            else
                RemoteHttpMontaguApiClient(appConfig)

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
                        throw MontaguAPIException("Response had errors ${result.errors.joinToString(",") { it.toString() }}", response.code)
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
        headers.forEach { k, v ->  headersBuilder.add(k, v)}

        val request = Request.Builder()
                .url(url)
                .headers(headersBuilder.build())
                .build()

        return client.newCall(request).execute()
    }

    protected abstract fun getHttpClient(): OkHttpClient
}

class LocalOkHttpMontaguApiClient(appConfig: Config): OkHttpMontaguAPIClient(appConfig)
{
    override fun getHttpClient(): OkHttpClient
    {
        //Stolen from https://stackoverflow.com/questions/25509296/trusting-all-certificates-with-okhttp
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
        })

        val allHostnameVerifier = object : HostnameVerifier{
            override fun verify(var1: String, var2: SSLSession): Boolean
            { return true }
        }

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory

        return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier(allHostnameVerifier)
                .build()
    }
}

class RemoteHttpMontaguApiClient(appConfig: Config): OkHttpMontaguAPIClient(appConfig)
{
    override fun getHttpClient(): OkHttpClient
    {
        return OkHttpClient()
    }
}

class MontaguAPIException(override val message: String, val status: Int) : IOException(message)
