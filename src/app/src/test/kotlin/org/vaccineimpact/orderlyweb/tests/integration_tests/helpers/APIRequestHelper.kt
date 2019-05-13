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

class APIRequestHelper: RequestHelper()
{
    init
    {
        CertificateHelper.disableCertificateValidation()
    }

    val apiBaseUrl: String = "http://localhost:${AppConfig()["app.port"]}/api/v1"

    private val parser = JsonParser()

    fun get(url: String, contentType: String = ContentTypes.json,
            userEmail: String = fakeGlobalReportReader()): Response
    {
        val token = generateToken(userEmail)
        val headers = standardHeaders(contentType).withAuthorizationHeader(token)
        return get(apiBaseUrl + url, headers)
    }

    fun getWithCookie(
            url: String,
            contentType: String = ContentTypes.json,
            userEmail: String = fakeGlobalReportReader()
    ): Response
    {
        val token = generateToken(userEmail)
        val cookieName = JWTCookieClient.cookie
        val headers = standardHeaders(contentType) +
                mapOf("Cookie" to "$cookieName=$token")
        return get( apiBaseUrl + url, headers)
    }

    fun post(url: String, body: Map<String, String>, contentType: String = ContentTypes.json,
             userEmail: String = fakeGlobalReportReader()): Response
    {
        val token = generateToken(userEmail)
        val headers = standardHeaders(contentType).withAuthorizationHeader(token)
        return khttp.post(apiBaseUrl + url, headers, json = body)
    }

    fun generateOnetimeToken(url: String, userEmail: String = fakeGlobalReportReader()): String
    {
        val response = get("/onetime_token/?url=/api/v1$url", userEmail = userEmail)
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
        return get(apiBaseUrl + url, headers)
    }

    fun getWrongPermissions(url: String, contentType: String = ContentTypes.json): Response
    {
        val token = generateToken("bademail@gmail.com")
        val headers = standardHeaders(contentType).withAuthorizationHeader(token)
        return get(apiBaseUrl + url, headers)
    }

    fun getNoAuth(url: String, contentType: String = ContentTypes.json): Response
    {
        return get(apiBaseUrl + url, standardHeaders(contentType))
    }

    private fun Map<String, String>.withAuthorizationHeader(token: String) = this +
            mapOf("Authorization" to "Bearer $token")

    private fun generateToken(emailAddress: String) =
            WebTokenHelper.instance.issuer.generateBearerToken(emailAddress)

}

fun fakeReportReader(reportName: String, addReport: Boolean = true): String
{
    val email = "report.reader@email.com"
    insertUser(email, "report reader")

    if (addReport)
    {
        insertReport(reportName, "v1")
    }
    giveUserGroupPermission(email, "reports.read", Scope.Specific("report", reportName))
    return email
}

fun fakeGlobalReportReader(): String
{
    val email = "global.report.reader@email.com"
    insertUser(email, "report reader")
    giveUserGroupPermission(email, "reports.read", Scope.Global())
    return email
}

fun fakeGlobalReportReviewer(): String
{
    val email = "global.report.reviewer@email.com"
    insertUser(email, "report reviewer")
    giveUserGroupPermission(email, "reports.read", Scope.Global())
    giveUserGroupPermission(email, "reports.review", Scope.Global())
    giveUserGroupPermission(email, "reports.run", Scope.Global())
    return email
}

fun fakeNoPermssionsUser(): String
{
    val email = "no.permissions@email.com"
    insertUser(email, "no permissions")
    return email
}