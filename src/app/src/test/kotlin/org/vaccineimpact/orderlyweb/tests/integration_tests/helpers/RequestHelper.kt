package org.vaccineimpact.orderlyweb.tests.integration_tests.helpers

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.test_helpers.http.BasicAuthorization
import org.vaccineimpact.orderlyweb.test_helpers.http.HttpClient

abstract class RequestHelper
{
    init
    {
        CertificateHelper.disableCertificateValidation()
    }

    abstract val baseUrl: String

    val MontaguTestUser = "test.user@example.com"

    protected fun standardHeaders(contentType: String) = mapOf("Accept" to contentType)

    fun loginWithMontagu(): JsonObject
    {
        // these user login details are set up in ./dev/run-dependencies.sh
        val auth = BasicAuthorization(MontaguTestUser, "password")
        val response = HttpClient.post("${AppConfig()["montagu.api_url"]}/authenticate/",
                data = mapOf("grant_type" to "client_credentials"),
                auth = auth
        )
        val text = response.text
        println(text)
        return Parser().parse(StringBuilder(text)) as JsonObject
    }
}
