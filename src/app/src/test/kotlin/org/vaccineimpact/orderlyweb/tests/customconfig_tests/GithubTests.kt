package org.vaccineimpact.orderlyweb.tests.customconfig_tests

import com.github.fge.jackson.JsonLoader
import khttp.post
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.test_helpers.GithubTokenHeader
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.RequestHelper

class GithubTests : CustomConfigTests()
{
    val apiBaseUrl: String = "http://localhost:${AppConfig()["app.port"]}/api/v1"
    val url = "$apiBaseUrl/login/"

    // this is a PAT for a test user who only has access to a test org with no repos
    // reversed so GitHub doesn't spot it and invalidate it
    val validGitHubToken = "db5920039c7d88fd976cbdab1da8e531c1148fcf".reversed()

    @Test
    fun `can login when fine-grained permissions are turned off`()
    {
        startApp("auth.fine_grained=false")

        val result = post(url, auth = GithubTokenHeader(validGitHubToken))

        assertSuccessful(result)

        val json = JsonLoader.fromString(result.text)
        assertThat(json["token_type"].textValue()).isEqualTo("bearer")
        assertThat(json["access_token"]).isNotNull
    }

    @Test
    fun `can login when fine-grained permissions are turned on`()
    {
        startApp("auth.fine_grained=true")

        val result = post(url, auth = GithubTokenHeader(validGitHubToken))

        assertSuccessful(result)

        val json = JsonLoader.fromString(result.text)
        assertThat(json["token_type"].textValue()).isEqualTo("bearer")
        assertThat(json["access_token"]).isNotNull
    }

    @Test
    fun `no permissions are required when fine-grained permissions are turned off`()
    {
        startApp("auth.fine_grained=false")

        val response = RequestHelper().get("/reports/minimal", userEmail = "test.user@example.com")
        assertSuccessful(response)
    }

    @Test
    fun `permissions are required when fine-grained permissions are turned on`()
    {
        startApp("auth.fine_grained=true")

        val response = RequestHelper().get("/reports/minimal", userEmail = "test.user@example.com")
        assertThat(response.statusCode).isEqualTo(403)
    }

    @Test
    fun `BadConfiguration errors are returned to the client`()
    {
        startApp("auth.github_org=hdyeiksn")

       val result = khttp.post("${RequestHelper().apiBaseUrl}/login",
               auth = GithubTokenHeader(validGitHubToken))

        assertThat(result.statusCode).isEqualTo(500)
        assertThat(result.text).contains("GitHub org hdyeiksn does not exist")
    }

}