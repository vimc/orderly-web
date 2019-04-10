package org.vaccineimpact.orderlyweb.customConfigTests

import khttp.get
import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.openqa.selenium.By
import org.vaccineimpact.orderlyweb.db.AppConfig

class GithubWebTests : SeleniumTest()
{
    val url: String = "http://localhost:${AppConfig()["app.port"]}/"
    val loginUrl = "${url}login/"

    @Test
    fun `can log in with Github`()
    {
        startApp("auth.provider=github")

        driver.get(url)
        val loginField = driver.findElement(By.id("login_field"))
        val passwordField = driver.findElement(By.id("password"))
        val pw = "AfakeP@s5w0rd"
        val username = "vimc-auth-test-user"

        loginField.sendKeys(username)
        passwordField.sendKeys(pw)

        driver.findElement(By.name("commit")).click()

        val header = driver.findElement(By.cssSelector("h1"))
        assertThat(header.text).isEqualTo("All reports")
    }

    @Test
    fun `login with invalid code and secret fails`()
    {
        startApp("auth.provider=github")

        //This spoofs the callback by github after user has provided valid credentials
        val fullUrl = "$loginUrl?code=fake&secret=fake"

        driver.get(fullUrl)
        val header = driver.findElements(By.ByCssSelector("h1")).first()

        assertThat(header.text).isEqualTo("Something went wrong")

        val response = get(url, allowRedirects = false)
        assertThat(response.statusCode).isNotEqualTo(200)

    }
}