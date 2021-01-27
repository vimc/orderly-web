package org.vaccineimpact.orderlyweb.customConfigTests

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.security.WebTokenHelper
import org.vaccineimpact.orderlyweb.test_helpers.http.BasicAuthorization
import org.vaccineimpact.orderlyweb.test_helpers.http.Response
import org.vaccineimpact.orderlyweb.test_helpers.http.HttpClient

class RequestHelper
{
    companion object
    {
        val apiBaseUrl: String = "http://localhost:${AppConfig()["app.port"]}/api/v1"
        val webBaseUrl: String = "http://localhost:${AppConfig()["app.port"]}"
    }

    fun get(url: String, contentType: String = ContentTypes.json,
            userEmail: String): Response
    {
        val token = generateToken(userEmail)
        val headers = standardHeaders(contentType).withAuthorizationHeader(token)
        return get(apiBaseUrl + url, headers)
    }

    fun loginWithMontagu(): JsonObject
    {
        // these user login details are set up in ./dev/run-dependencies.sh
        val auth = BasicAuthorization("test.user@example.com", "password")
        val response = HttpClient.post("${AppConfig()["montagu.api_url"]}/authenticate/",
                data = mapOf("grant_type" to "client_credentials"),
                auth = auth
        )
        val text = response.text
        println(text)
        return Parser().parse(StringBuilder(text)) as JsonObject
    }

    private fun standardHeaders(contentType: String) = mapOf("Accept" to contentType)

    private fun Map<String, String>.withAuthorizationHeader(token: String) = this +
            mapOf("Authorization" to "Bearer $token")

    private fun get(url: String, headers: Map<String, String>) = HttpClient.get(url, headers)

    private fun generateToken(emailAddress: String) =
            WebTokenHelper.instance.issuer.generateBearerToken(emailAddress)

}
