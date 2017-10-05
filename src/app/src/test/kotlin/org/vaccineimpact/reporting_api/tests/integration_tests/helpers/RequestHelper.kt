package org.vaccineimpact.reporting_api.tests.integration_tests.helpers

import com.github.salomonbrys.kotson.get
import com.google.gson.JsonParser
import khttp.responses.Response
import org.assertj.core.api.Assertions
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.security.MontaguUser
import org.vaccineimpact.reporting_api.tests.integration_tests.APITests

class RequestHelper
{
    init
    {
        CertificateHelper.disableCertificateValidation()
    }

    private val baseUrl: String = "http://localhost:${Config["app.port"]}/v1"
    private val parser = JsonParser()

    val fakeUser = MontaguUser("tettusername", "user", "*/reports.read")
    val fakeReviewer = MontaguUser("testreviewer", "reports-reviewer", "*/reports.read,*/reports.review, */reports.run")

    fun get(url: String, contentType: String = ContentTypes.json, reviewer: Boolean = false): Response
    {
        var headers = mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )

        val token = if (!reviewer)
        {
            APITests.tokenHelper
                    .generateToken(fakeUser)
        }
        else
        {
            APITests.tokenHelper
                    .generateToken(fakeReviewer)
        }

        headers += mapOf("Authorization" to "Bearer $token")

        return get(baseUrl + url, headers)
    }

    fun post(url: String, body: Map<String, String>, contentType: String = ContentTypes.json, reviewer: Boolean = false): Response
    {
        var headers = mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )

        val token = if (!reviewer)
        {
            APITests.tokenHelper
                    .generateToken(fakeUser)
        }
        else
        {
            APITests.tokenHelper
                    .generateToken(fakeReviewer)
        }

        headers += mapOf("Authorization" to "Bearer $token")

        return khttp.post(baseUrl + url, headers, json = body)
    }

    fun generateOnetimeToken(url: String): String
    {
        val response = get("/onetime_token/?url=/v1$url")
        val json = parser.parse(response.text)
        if (json["status"].asString != "success")
        {
            Assertions.fail("Failed to get onetime token. Result from API was:" + response.text)
        }
        return json["data"].asString
    }

    fun getWrongAuth(url: String, contentType: String = ContentTypes.json): Response
    {
        var headers = mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )

        val token = "faketoken"
        headers += mapOf("Authorization" to "Bearer $token")

        return get(baseUrl + url, headers)
    }

    fun getWrongPermissions(url: String, contentType: String = ContentTypes.json): Response
    {
        var headers = mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )

        val token = APITests.tokenHelper.generateToken(MontaguUser("tettusername", "user", "*/fake-perm"))

        headers += mapOf("Authorization" to "Bearer $token")

        return get(baseUrl + url, headers)
    }

    fun getNoAuth(url: String, contentType: String = ContentTypes.json): Response
    {
        val headers = mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )

        return get(baseUrl + url, headers)
    }

    private fun get(url: String, headers: Map<String, String>)
            = khttp.get(url, headers)
}