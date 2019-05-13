package org.vaccineimpact.orderlyweb.tests.integration_tests.helpers

import khttp.responses.Response
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClient
import spark.route.HttpMethod

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

    fun requestWithSessionCookie(url: String,
                         cookies: String,
                         contentType: String = "text/html",
                         method: HttpMethod = HttpMethod.get): Response
    {
        val headers = standardHeaders(contentType) + mapOf("Cookie" to cookies)
        val fullUrl = webBaseUrl + url
        when (method)
        {
            HttpMethod.get -> return khttp.get(fullUrl, headers)
            HttpMethod.post -> return khttp.post(fullUrl, headers)
        }
    }

    fun loginWithMontaguAndMakeRequest(url: String, contentType: String = "text/html",
                                       method: HttpMethod = HttpMethod.get): Response
    {
        val sessionCookie = webLoginWithMontagu()
        return requestWithSessionCookie(url, sessionCookie, contentType, method)
    }

}