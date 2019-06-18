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
import javax.net.ssl.TrustManager
import java.io.BufferedInputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import java.io.FileInputStream


interface MontaguAPIClient
{
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

class okhttpMontaguAPIClient(appConfig: Config = AppConfig()) : MontaguAPIClient
{
    private val urlBase = appConfig["montagu.api_url"]
    private val devMode = appConfig["proxy.dev.mode"]
    private val serializer = Serializer.instance.gson

    override fun getUserDetails(token: String): MontaguAPIClient.UserDetails
    {
        //val response = khttp.get("$urlBase/user/",
        //        headers = mapOf("Authorization" to "Bearer $token"))

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
        val client: OkHttpClient;
        if (devMode != "true")
        {
            client = OkHttpClient()
        }
        else
        {
            //If in dev mode we need to allow the use of our self-signed certificate

            //Stolen from https://jebware.com/blog/?p=340
            val trustManagers: Array<TrustManager>

            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            val certInputStream = FileInputStream("/etc/orderly/web/certificate.pem")
            val bis = BufferedInputStream(certInputStream)
            val certificateFactory = CertificateFactory.getInstance("X.509")
            while (bis.available() > 0)
            {
                val cert = certificateFactory.generateCertificate(bis)
                keyStore.setCertificateEntry("www.example.com", cert)
            }
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)
            trustManagers = trustManagerFactory.getTrustManagers()
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustManagers, null)

            client = OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), trustManagers[0] as X509TrustManager)
                    .build()
        }

        val headersBuilder = Headers.Builder()
        headers.forEach { k, v ->  headersBuilder.add(k, v)}

        val request = Request.Builder()
                .url(url)
                .headers(headersBuilder.build())
                .build()

        return client.newCall(request).execute()
    }
}

class MontaguAPIException(override val message: String, val status: Int) : IOException(message)
