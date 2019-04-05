package org.vaccineimpact.orderlyweb.customConfigTests

import khttp.get
import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.AppConfig


class GithubWebTests : CustomConfigTests()
{
    val url: String = "http://localhost:${AppConfig()["app.port"]}/"
    val loginUrl = "${url}login/"

    @Test
    fun `can redirect when not logged in`()
    {
        startApp("auth.provider=github")

        val response = get(url)

        //khttp does redirection for you so we expect the response from github, not the original 302
        Assertions.assertThat(response.statusCode).isEqualTo(200)
        Assertions.assertThat(response.url).startsWith("https://github.com/login")
        Assertions.assertThat(response.request.params["client_id"]).isEqualTo(AppConfig()["auth.github_key"])

    }
}