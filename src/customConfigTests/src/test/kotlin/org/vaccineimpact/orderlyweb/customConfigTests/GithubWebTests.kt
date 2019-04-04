package org.vaccineimpact.orderlyweb.customConfigTests

import com.github.fge.jackson.JsonLoader
import khttp.post
import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.test_helpers.GithubTokenHeader


class GithubWebTests : CustomConfigTests()
{
    val baseUrl: String = "http://localhost:${AppConfig()["app.port"]}"
    val url = "$baseUrl/login/"

    @Test
    fun `can redirect when not logged in`()
    {
        startApp("auth.provider=github")

        val response = post(url)

        Assertions.assertThat(response.statusCode).isEqualTo(302)
    }
}