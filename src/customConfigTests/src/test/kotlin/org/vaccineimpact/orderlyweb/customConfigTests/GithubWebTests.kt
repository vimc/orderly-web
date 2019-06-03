package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.openqa.selenium.By
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.test_helpers.insertReport

class GithubWebTests : SeleniumTest()
{
    val url: String = "http://localhost:${AppConfig()["app.port"]}/"
    val loginUrl = "${url}login/"

    private fun login()
    {
        driver.get(url)

        clickOnLandingPageLink()

        val loginField = driver.findElement(By.id("login_field"))
        val passwordField = driver.findElement(By.id("password"))

        val pw = "notarealpassword"
        val username = "notarealuser"

        loginField.sendKeys(username)
        passwordField.sendKeys(pw)

        driver.findElement(By.name("commit")).click()
    }

    @Test
    fun `can log in with Github`()
    {
        startApp("auth.provider=github\nauth.fine_grained=false")
        login()

        val header = driver.findElement(By.cssSelector("h1"))
        assertThat(header.text).isEqualTo("Find a report")
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

        driver.get(url)
        assertThat(driver.currentUrl).contains(url + "weblogin")
    }

    @Test
    fun `user sees 401 page if not in configured org`()
    {
        startApp("auth.provider=github\nauth.github_org=vimc")

        login()

        val helpText = driver.findElements(By.cssSelector("p")).first()
        assertThat(helpText.text)
                .contains("We have not been able to successfully identify you as a member of the app's configured Github org")

    }

    @Test
    fun `users cannot see pages they do not have permissions for`()
    {
        startApp("auth.provider=github")
        insertReport("testreport", "20170103-143015-1234abcd")

        login()

        // this route requires report.read and by default the test user has no permissions
        driver.get(RequestHelper.webBaseUrl + "/reports/testreport/20170103-143015-1234abcd/")
        assertThat(driver.findElement(By.cssSelector("h1")).text).isEqualTo("Page not found")
    }
}