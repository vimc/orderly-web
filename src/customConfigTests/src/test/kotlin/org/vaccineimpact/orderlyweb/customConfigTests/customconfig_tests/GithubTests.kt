package org.vaccineimpact.orderlyweb.customConfigTests.customconfig_tests
import com.github.fge.jackson.JsonLoader
import khttp.post
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.test_helpers.GithubTokenHeader

class GithubTests : CustomConfigTests()
{
    val apiBaseUrl: String = "http://localhost:${AppConfig()["app.port"]}/api/v1"
    val url = "$apiBaseUrl/login/"

    @Test
    fun `can login when fine-grained permissions are turned off`()
    {
        startApp("auth.fine_grained=false")

        // this is a PAT for a test user who only has access to a test org with no repos
        // reversed so GitHub doesn't spot it and invalidate it
        val token = "db5920039c7d88fd976cbdab1da8e531c1148fcf".reversed()

        val result = post(url, auth = GithubTokenHeader(token))

        assertSuccessful(result)

        val json = JsonLoader.fromString(result.text)
        assertThat(json["token_type"].textValue()).isEqualTo("bearer")
        assertThat(json["access_token"]).isNotNull
    }

    @Test
    fun `can login when fine-grained permissions are turned on`()
    {
        startApp("auth.fine_grained=true")

        // this is a PAT for a test user who only has access to a test org with no repos
        // reversed so GitHub doesn't spot it and invalidate it
        val token = "db5920039c7d88fd976cbdab1da8e531c1148fcf".reversed()

        val result = post(url, auth = GithubTokenHeader(token))

        assertSuccessful(result)

        val json = JsonLoader.fromString(result.text)
        assertThat(json["token_type"].textValue()).isEqualTo("bearer")
        assertThat(json["access_token"]).isNotNull
    }
}