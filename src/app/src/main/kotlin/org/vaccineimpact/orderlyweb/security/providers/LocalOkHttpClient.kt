package org.vaccineimpact.orderlyweb.security.providers

import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.*

fun getLocalOkHttpClient(): OkHttpClient
{
    // Stolen from https://stackoverflow.com/questions/25509296/trusting-all-certificates-with-okhttp
    // Create a trust manager that does not validate certificate chains
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager
    {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            // do nothing
        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            // do nothing
        }

        override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
    })

    val allHostnameVerifier = object : HostnameVerifier
    {
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
