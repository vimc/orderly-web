package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.auth

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import khttp.structures.authorization.BasicAuthorization
import org.assertj.core.api.Assertions
import org.junit.Assert.assertThat
import org.junit.Ignore
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.security.authentication.khttpMontaguAPIClient
import org.vaccineimpact.orderlyweb.test_helpers.MontaguTests

class MontaguAuthenticationTests : MontaguTests()
{
    @Test
    fun `can talk to API`()
    {
        val token = login()["access_token"].toString()
        val sut = khttpMontaguAPIClient()
        val result = sut.getUserDetails(token)
    }

    @Ignore
    @Test
    fun `can get user details`()
    {
        val token = login()["access_token"].toString()
        val sut = khttpMontaguAPIClient()
        val result = sut.getUserDetails(token)
        Assertions.assertThat(result.username).isEqualTo("test.user")
        Assertions.assertThat(result.email).isEqualTo("test.user@example.com")
    }

    fun login(): JsonObject
    {
        // these user login details are set up in ./dev/run-dependencies.sh
        val auth = BasicAuthorization("test.user@example.com", "password")
        val response = khttp.post(AppConfig()["montagu.api_url"],
                data = mapOf("grant_type" to "client_credentials"),
                auth = auth
        )
        val text = response.text
        println(text)
        return Parser().parse(StringBuilder(text)) as JsonObject
    }

}