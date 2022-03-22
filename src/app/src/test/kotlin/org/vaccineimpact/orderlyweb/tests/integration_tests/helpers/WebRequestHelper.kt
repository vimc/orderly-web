package org.vaccineimpact.orderlyweb.tests.integration_tests.helpers

import com.google.gson.GsonBuilder
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClient
import org.vaccineimpact.orderlyweb.test_helpers.http.HttpClient
import org.vaccineimpact.orderlyweb.test_helpers.http.Response
import spark.route.HttpMethod

class WebRequestHelper : RequestHelper()
{
    override val baseUrl: String = "http://localhost:${AppConfig()["app.port"]}"
    val authRepo = OrderlyAuthorizationRepository()
    val userRepo = OrderlyUserRepository()

    fun getWebPage(
            url: String,
            contentType: String = "text/html"
    ): Response
    {
        val headers = standardHeaders(contentType)
        return HttpClient.get(baseUrl + url, headers, true)
    }

    fun loginWithMontaguAndMakeRequest(url: String,
                                       withPermissions: Set<ReifiedPermission>,
                                       contentType: String = "text/html",
                                       method: HttpMethod = HttpMethod.get,
                                       postData: Map<String, Any>): Response
    {
        val sessionCookie = webLoginWithMontagu(withPermissions)
        return requestWithSessionCookie(url, sessionCookie, contentType, method, postData)
    }

    fun loginWithMontaguAndMakeRequest(url: String,
                                       withPermissions: Set<ReifiedPermission>,
                                       contentType: String = "text/html",
                                       method: HttpMethod = HttpMethod.get,
                                       data: String? = null): Response
    {
        val sessionCookie = webLoginWithMontagu(withPermissions)
        return requestWithSessionCookie(url, sessionCookie, contentType, method, data)
    }

    fun getWithMontaguCookie(
            url: String,
            montaguToken: String,
            contentType: String = "text/html",
            allowRedirects: Boolean = false
    ): Response
    {
        val cookieName = MontaguIndirectClient.cookie
        val headers = standardHeaders(contentType) +
                mapOf("Cookie" to "$cookieName=$montaguToken")
        return HttpClient.get(baseUrl + url, headers, allowRedirects = allowRedirects)
    }

    fun webLoginWithMontagu(withPermissions: Set<ReifiedPermission> = setOf()): String
    {
        userRepo.addUser(MontaguTestUser, "test.user", "Test User", UserSource.CLI)
        withPermissions.forEach {
            authRepo.ensureUserGroupHasPermission(MontaguTestUser, it)
        }
        val montaguToken = loginWithMontagu()["access_token"] as String
        val loginResponse = getWithMontaguCookie("/login/", montaguToken)
        return loginResponse.headers["set-cookie"].toString()
    }

    fun requestWithSessionCookie(url: String,
                                 cookies: String,
                                 contentType: String = "text/html",
                                 method: HttpMethod = HttpMethod.get,
                                 postData: Map<String, Any>): Response
    {
        return requestWithSessionCookie(url, cookies, contentType, method, postData.toJson())
    }

    fun requestWithSessionCookie(url: String,
                                 cookies: String,
                                 contentType: String = "text/html",
                                 method: HttpMethod = HttpMethod.get,
                                 data: String? = null,
                                 additionalHeaders: Map<String, String> = mapOf()): Response
    {
        val headers = standardHeaders(contentType) + mapOf("Cookie" to cookies) + additionalHeaders
        val fullUrl = if (url.contains("http"))
        {
            url
        }
        else
        {
            "$baseUrl/$url"
        }
        return when (method)
        {
            HttpMethod.get -> HttpClient.get(fullUrl, headers)
            HttpMethod.post -> HttpClient.post(fullUrl, headers, data = data)
            HttpMethod.delete -> HttpClient.delete(fullUrl, headers)
            else -> throw IllegalArgumentException("Method not supported")
        }
    }

    private fun Map<String, *>.toJson(): String
    {
        return GsonBuilder().create().toJson(this)
    }
}
