package org.vaccineimpact.orderlyweb.tests.integration_tests.helpers

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import khttp.structures.authorization.BasicAuthorization
import org.vaccineimpact.orderlyweb.db.AppConfig

abstract class RequestHelper
{
    init
    {
        CertificateHelper.disableCertificateValidation()
    }

    abstract val baseUrl: String

    val MontaguTestUser = "test.user@example.com"

    protected fun get(url: String, headers: Map<String, String>) = khttp.get(url, headers)

    protected fun standardHeaders(contentType: String): Map<String, String>
    {
        return mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )
    }

    fun loginWithMontagu(): JsonObject
    {
        // these user login details are set up in ./dev/run-dependencies.sh
        val auth = BasicAuthorization(MontaguTestUser, "password")
        val response = khttp.post("${AppConfig()["montagu.api_url"]}/authenticate/",
                data = mapOf("grant_type" to "client_credentials"),
                auth = auth
        )
        val text = response.text
        return Parser().parse(StringBuilder(text)) as JsonObject
    }
}