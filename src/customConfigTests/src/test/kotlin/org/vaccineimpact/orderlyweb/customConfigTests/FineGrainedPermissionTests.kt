package org.vaccineimpact.orderlyweb.customConfigTests

import com.github.fge.jackson.JsonLoader
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.test_helpers.TestTokenHeader
import org.vaccineimpact.orderlyweb.test_helpers.http.HttpClient

class FineGrainedPermissionTests : CustomConfigTests()
{
    val apiBaseUrl: String = "http://localhost:${AppConfig()["app.port"]}/api/v1"
    val url = "$apiBaseUrl/login/"

    @Test
    fun `can login when fine-grained permissions are turned off`()
    {
        startApp("auth.fine_grained=false")

        val token = RequestHelper().loginWithMontagu()["access_token"].toString()
        val result = HttpClient.post(url, auth = TestTokenHeader(token))

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
        Assertions.assertThat(response.statusCode).isEqualTo(403)
    }

    @Test
    fun `can login when fine-grained permissions are turned on`()
    {
        startApp("auth.fine_grained=true")

        val token = RequestHelper().loginWithMontagu()["access_token"].toString()

        val result = HttpClient.post(url, auth = TestTokenHeader(token))

        assertSuccessful(result)

        val json = JsonLoader.fromString(result.text)
        assertThat(json["token_type"].textValue()).isEqualTo("bearer")
        assertThat(json["access_token"]).isNotNull
    }
}
