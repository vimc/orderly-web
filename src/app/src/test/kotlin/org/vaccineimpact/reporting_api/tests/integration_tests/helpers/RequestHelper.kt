package org.vaccineimpact.reporting_api.tests.integration_tests.helpers

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import khttp.responses.Response
import org.vaccineimpact.api.models.Scope
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.api.models.permissions.ReifiedRole
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.security.KeyHelper
import org.vaccineimpact.reporting_api.security.MontaguUser
import org.vaccineimpact.reporting_api.security.UserProperties
import org.vaccineimpact.reporting_api.security.WebTokenHelper

class RequestHelper {
    init {
        CertificateHelper.disableCertificateValidation()
    }

    fun get(url: String, contentType: String = ContentTypes.json): Response {
        var headers = mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )

        val token = WebTokenHelper(KeyHelper.keyPair).generateToken(MontaguUser(UserProperties("tettusername", "Test User", "testemail", "testUserPassword", null),
                listOf(ReifiedRole("rolename", Scope.Global())), listOf(ReifiedPermission("can-read-reports", Scope.Global()))))
        headers += mapOf("Authorization" to "Bearer $token")

        return get("http://localhost:8080/v1" + url, headers)
    }

    fun getWrongPermissions(url: String, contentType: String = ContentTypes.json): Response {
        var headers = mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )

        val token = WebTokenHelper(KeyHelper.keyPair).generateToken(MontaguUser(UserProperties("tettusername", "Test User", "testemail", "testUserPassword", null),
                listOf(ReifiedRole("rolename", Scope.Global())), listOf(ReifiedPermission("fake-perm", Scope.Global()))))

        headers += mapOf("Authorization" to "Bearer $token")

        return get("http://localhost:8080/v1" + url, headers)
    }

    fun getNoAuth(url: String, contentType: String = ContentTypes.json): Response {
        val headers = mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )

        return get("http://localhost:8080/v1" + url, headers)
    }

    private fun get(url: String, headers: Map<String, String>)
            = khttp.get(url, headers)
}

fun <T> Response.montaguData(): T? {
    val data = this.json()["data"]
    if (data != "") {
        @Suppress("UNCHECKED_CAST")
        return data as T
    } else {
        return null
    }
}

fun Response.json() = Parser().parse(StringBuilder(text)) as JsonObject