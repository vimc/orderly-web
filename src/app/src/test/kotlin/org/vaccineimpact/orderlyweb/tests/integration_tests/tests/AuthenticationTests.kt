package org.vaccineimpact.orderlyweb.tests.integration_tests.tests

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.json
import khttp.options
import khttp.post
import khttp.responses.Response
import khttp.structures.authorization.BasicAuthorization
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.RequestHelper

class AuthenticationTests : IntegrationTest()
{
    @Test
    fun `authentication fails without BasicAuth header`()
    {
        val result = post("email@example.com", "password", includeAuth = false)
        assertDoesNotAuthenticate(result)
    }

    @Test
    fun `can get OPTIONS for authentication endpoint`()
    {
        val result = options(url)
        assertThat(result.statusCode).isEqualTo(200)
    }

    private fun assertDoesAuthenticate(result: JsonObject)
    {
        assertThat(result).doesNotContainKey("error")
        assertThat(result).containsKey("access_token")
        assertThat(result["token_type"]).isEqualTo("bearer")
        assertThat(isLong(result["expires_in"].toString()))
    }

    private fun assertDoesNotAuthenticate(result: JsonObject)
    {
        assertThat(result).isEqualTo(json {
            obj(
                    "data" to "",
                    "errors" to array(obj("code" to "login-failed", "message" to "invalid_client")),
                    "status" to "failure"

            )
        })
    }

    private fun isLong(raw: String): Boolean
    {
        try
        {
            raw.toLong()
            return true
        }
        catch (e: NumberFormatException)
        {
            return false
        }
    }

    private fun checkCookieAndGetValue(response: Response, key: String): String
    {
        val cookie = response.cookies.getCookie(key)
                ?: throw Exception("No cookie with key '$key' was found in response: ${response.text}")
        assertThat(cookie.attributes).containsKey("HttpOnly")
        assertThat(cookie.attributes["SameSite"]).isEqualTo("Strict")
        return cookie.value as String
    }

    companion object
    {
        val url = "${RequestHelper().baseUrl}/login/"

        fun post(username: String, token: String, includeAuth: Boolean = true): JsonObject
        {
            val auth = if (includeAuth) BasicAuthorization(username, token) else null
            val response = post(url,
                    data = mapOf("grant_type" to "client_credentials"),
                    auth = auth
            )
            val text = response.text
            println(text)
            return Parser().parse(StringBuilder(text)) as JsonObject
        }
    }
}