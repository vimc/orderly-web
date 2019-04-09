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
    fun `can redirect to Github when not logged in`()
    {
        startApp("auth.provider=github")

        val response = get(url, allowRedirects = false)

        Assertions.assertThat(response.statusCode).isEqualTo(302)

        val redirectUrl = response.headers["Location"]!!

        //check redirecting to github login, with correct gitub app client id
        Assertions.assertThat(redirectUrl).startsWith("https://github.com/login")

        val match = "client_id=([^&]*)".toRegex().find(redirectUrl)
        Assertions.assertThat(match!!.groups[1]!!.value).isEqualTo(AppConfig()["auth.github_key"])

    }

    @Test
    fun `login with invalid code and secret fails`()
    {
        startApp("auth.provider=github")

        //This spoofs the callback by github after user has provided valid credentials
        val fullUrl = "${loginUrl}?code=fake&secret=fake"

        val response = get(fullUrl, allowRedirects=false)

        Assertions.assertThat(response.statusCode).isNotEqualTo(200)
    }
}