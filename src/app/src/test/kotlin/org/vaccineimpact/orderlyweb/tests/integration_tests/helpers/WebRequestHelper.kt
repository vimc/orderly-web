package org.vaccineimpact.orderlyweb.tests.integration_tests.helpers

import khttp.responses.Response
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClient

class WebRequestHelper: RequestHelper()
{
    init
    {
        CertificateHelper.disableCertificateValidation()
    }

    val webBaseUrl: String = "http://localhost:${AppConfig()["app.port"]}"

    fun getWebPage(
            url: String,
            contentType: String = "text/html"
    ): Response
    {
        val headers = standardHeaders(contentType)
        return get(webBaseUrl + url, headers)
    }

    fun getWithMontaguCookie(
            url: String,
            montaguToken: String,
            contentType: String = "text/html"
    ): Response
    {
        val cookieName = MontaguIndirectClient.cookie
        val headers = standardHeaders(contentType) +
                mapOf("Cookie" to "$cookieName=$montaguToken")
        return khttp.get(webBaseUrl + url, headers, allowRedirects = false)
    }

    fun webLoginWithMontagu(): String
    {
        val montaguToken = loginWithMontagu()["access_token"] as String
        val loginResponse = getWithMontaguCookie("/login/", montaguToken)
        return loginResponse.headers["Set-Cookie"].toString()
    }

    fun getWithSessionCookie(url: String,
                         cookies: String,
                         contentType: String = "text/html"): Response
    {
        val headers = standardHeaders(contentType) + mapOf("Cookie" to cookies)
        return khttp.get(webBaseUrl + url, headers)
    }

    fun loginWithMontaguAndGet(url: String, contentType: String = "text/html"): Response
    {
        val sessionCookie = webLoginWithMontagu()
        return getWithSessionCookie(url, sessionCookie, contentType)
    }

}