package org.vaccineimpact.orderlyweb.customConfigTests

import khttp.get
import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.AppConfig


class GithubWebTests : CustomConfigTests()
{
    val url: String = "http://localhost:8888/"
    val loginUrl = "${url}login/"

    @Test
    fun `can redirect when not logged in`()
    {
        startApp("auth.provider=github")

        val response = get(url)

        Assertions.assertThat(response.text).isEqualTo("")
        Assertions.assertThat(response.statusCode).isEqualTo(302)
    }
}