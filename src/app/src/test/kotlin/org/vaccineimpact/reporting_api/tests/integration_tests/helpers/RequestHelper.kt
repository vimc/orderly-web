package org.vaccineimpact.reporting_api.tests.integration_tests.helpers

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import khttp.responses.Response
import org.vaccineimpact.api.models.Scope
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.api.models.permissions.ReifiedRole
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.security.MontaguUser
import org.vaccineimpact.reporting_api.security.UserProperties
import org.vaccineimpact.reporting_api.tests.integration_tests.APITests

class RequestHelper
{
    init
    {
        CertificateHelper.disableCertificateValidation()
    }

    private val baseUrl: String = "http://localhost:${Config["app.port"]}/v1"

    fun get(url: String, contentType: String = ContentTypes.json): Response
    {
        var headers = mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )

        val token = APITests.tokenHelper
                .generateToken(MontaguUser(UserProperties("tettusername", "Test User", "testemail", "testUserPassword", null),
                        listOf(ReifiedRole("rolename", Scope.Global())), listOf(ReifiedPermission("can-login", Scope.Global()))))

        headers += mapOf("Authorization" to "Bearer $token")

        return get(baseUrl + url, headers)
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

        val token = APITests.tokenHelper.generateToken(MontaguUser(UserProperties("tettusername", "Test User", "testemail", "testUserPassword", null),
                listOf(ReifiedRole("rolename", Scope.Global())), listOf(ReifiedPermission("fake-perm", Scope.Global()))))

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

fun <T> Response.montaguData(): T?
{
    val data = this.json()["data"]
    if (data != "")
    {
        @Suppress("UNCHECKED_CAST")
        return data as T
    }
    else
    {
        return null
    }
}

fun Response.json() = Parser().parse(StringBuilder(text)) as JsonObject