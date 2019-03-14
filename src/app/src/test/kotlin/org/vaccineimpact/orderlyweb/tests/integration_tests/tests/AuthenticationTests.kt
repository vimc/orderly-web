package org.vaccineimpact.orderlyweb.tests.integration_tests.tests

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.json
import khttp.options
import khttp.post
import khttp.structures.authorization.Authorization
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.RequestHelper
import java.util.*

class AuthenticationTests : IntegrationTest()
{
    @Test
    fun `authentication fails without GithubBasicAuth header`()
    {
        val result = post("username", "password", includeAuth = false)
        assertDoesNotAuthenticate(result)
    }

    @Test
    fun `authentication succeeds with custom GithubBasicAuth header`()
    {
        val result = post("username", "password")
        assertDoesAuthenticate(result)
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
                    "error" to "invalid_client"

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

    companion object
    {
        val url = "${RequestHelper().baseUrl}/login/"

        fun post(username: String, token: String, includeAuth: Boolean = true): JsonObject
        {
            val auth = if (includeAuth) GithubBasicAuthorization(username, token) else null
            val response = post(url,
                    data = mapOf("grant_type" to "client_credentials"),
                    auth = auth
            )
            val text = response.text
            return Parser().parse(StringBuilder(text)) as JsonObject
        }
    }

    data class GithubBasicAuthorization(val user: String, val password: String) : Authorization
    {
        override val header: Pair<String, String>
            get() {
                val b64 = Base64.getEncoder().encode("${this.user}:${this.password}".toByteArray()).toString(Charsets.UTF_8)
                return "Authorization" to "GithubBasic $b64"
            }
    }

}