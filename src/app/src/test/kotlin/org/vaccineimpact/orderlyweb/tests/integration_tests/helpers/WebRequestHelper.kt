package org.vaccineimpact.orderlyweb.tests.integration_tests.helpers

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.salomonbrys.kotson.get
import com.google.gson.JsonParser
import khttp.responses.Response
import khttp.structures.authorization.BasicAuthorization
import org.assertj.core.api.Assertions
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.security.WebTokenHelper
import org.vaccineimpact.orderlyweb.security.clients.JWTCookieClient
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClient
import org.vaccineimpact.orderlyweb.tests.giveUserGroupPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.insertUser

class WebRequestHelper: RequestHelper()
{
    init
    {
        CertificateHelper.disableCertificateValidation()
    }

    val webBaseUrl: String = "http://localhost:${AppConfig()["app.port"]}"

    val SESSION_COOKIE_NAME = "JSESSIONID"

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
        val sessionCookie = loginResponse.cookies[SESSION_COOKIE_NAME]!!
        return "montagu_jwt_token=$montaguToken; $SESSION_COOKIE_NAME=$sessionCookie"
    }

    fun getWithSessionCookie(url: String,
                         cookies: String,
                         contentType: String = "text/html"): Response
    {
        val headers = standardHeaders(contentType) + mapOf("Cookie" to cookies)

        return khttp.get(webBaseUrl + url, headers)
    }

}