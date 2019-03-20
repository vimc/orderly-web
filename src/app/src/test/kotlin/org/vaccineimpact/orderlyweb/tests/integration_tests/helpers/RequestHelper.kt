package org.vaccineimpact.orderlyweb.tests.integration_tests.helpers

import com.github.salomonbrys.kotson.get
import com.google.gson.JsonParser
import khttp.responses.Response
import org.assertj.core.api.Assertions
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.security.clients.JWTCookieClient
import org.vaccineimpact.orderlyweb.security.WebTokenHelper

class RequestHelper
{
    init
    {
        CertificateHelper.disableCertificateValidation()
    }

    val baseUrl: String = "http://localhost:${AppConfig()["app.port"]}/v1"
    private val parser = JsonParser()

    val fakeGlobalReportReader = "report.reader@email.com"
    val fakeReviewer = "report.reviewer@email.comn"

    fun get(url: String, contentType: String = ContentTypes.json, userEmail: String = fakeGlobalReportReader): Response
    {
        val token = generateToken(userEmail)
        val headers = standardHeaders(contentType).withAuthorizationHeader(token)
        return get(baseUrl + url, headers)
    }

    fun getWithCookie(
            url: String,
            contentType: String = ContentTypes.json,
            userEmail: String = fakeGlobalReportReader
    ): Response
    {
        val token = generateToken(userEmail)
        val cookieName = JWTCookieClient.cookie
        val headers = standardHeaders(contentType) +
                mapOf("Cookie" to "$cookieName=$token")
        return get(baseUrl + url, headers)
    }

    fun post(url: String, body: Map<String, String>, contentType: String = ContentTypes.json,
             userEmail: String = fakeGlobalReportReader): Response
    {
        val token = generateToken(userEmail)
        val headers = standardHeaders(contentType).withAuthorizationHeader(token)
        return khttp.post(baseUrl + url, headers, json = body)
    }

    fun generateOnetimeToken(url: String, userEmail: String = fakeGlobalReportReader): String
    {
        val response = get("/onetime_token/?url=/v1$url", userEmail = userEmail)
        val json = parser.parse(response.text)
        if (json["status"].asString != "success")
        {
            Assertions.fail("Failed to get onetime token. Result from API was:" + response.text)
        }
        return json["data"].asString
    }

    fun getWrongAuth(url: String, contentType: String = ContentTypes.json): Response
    {
        val token = "faketoken"
        val headers = standardHeaders(contentType).withAuthorizationHeader(token)
        return get(baseUrl + url, headers)
    }

    fun getWrongPermissions(url: String, contentType: String = ContentTypes.json): Response
    {
        val token = generateToken("bademail@gmail.com")
        val headers = standardHeaders(contentType).withAuthorizationHeader(token)
        return get(baseUrl + url, headers)
    }

    fun getNoAuth(url: String, contentType: String = ContentTypes.json): Response
    {
        return get(baseUrl + url, standardHeaders(contentType))
    }

    private fun standardHeaders(contentType: String): Map<String, String>
    {
        return mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )
    }

    private fun Map<String, String>.withAuthorizationHeader(token: String) = this +
            mapOf("Authorization" to "Bearer $token")

    private fun get(url: String, headers: Map<String, String>) = khttp.get(url, headers)

    private fun generateToken(emailAddress: String) =
            WebTokenHelper.instance.issuer.generateBearerToken(emailAddress)

}