package org.vaccineimpact.orderlyweb.tests.integration_tests.helpers

import khttp.responses.Response
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClient
import spark.route.HttpMethod

class WebRequestHelper : RequestHelper()
{
    override val baseUrl: String = "http://localhost:${AppConfig()["app.port"]}"
    val authRepo = OrderlyAuthorizationRepository()

    fun getWebPage(
            url: String,
            contentType: String = "text/html"
    ): Response
    {
        val headers = standardHeaders(contentType)
        return get(baseUrl + url, headers)
    }

    fun loginWithMontaguAndMakeRequest(url: String, contentType: String = "text/html",
                                       method: HttpMethod = HttpMethod.get): Response
    {
        val sessionCookie = webLoginWithMontagu()
        return requestWithSessionCookie(url, sessionCookie, contentType, method)
    }

    private fun getWithMontaguCookie(
            url: String,
            montaguToken: String,
            contentType: String = "text/html"
    ): Response
    {
        val cookieName = MontaguIndirectClient.cookie
        val headers = standardHeaders(contentType) +
                mapOf("Cookie" to "$cookieName=$montaguToken")
        return khttp.get(baseUrl + url, headers, allowRedirects = false)
    }

    fun webLoginWithMontagu(withPermissions: Set<ReifiedPermission> = setOf()): String
    {
        withPermissions.forEach{
            authRepo.ensureUserGroupHasPermission(MontaguTestUser, it)
        }
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
        val fullUrl = baseUrl + url
        val result = when (method)
        {
            HttpMethod.get -> khttp.get(fullUrl, headers)
            HttpMethod.post -> khttp.post(fullUrl, headers)
            else -> throw IllegalArgumentException("Method not supported")
        }

        return result
    }

}