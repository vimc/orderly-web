package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.vaccineimpact.orderlyweb.db.AppConfig

class AnonUserTests: SeleniumTest() {

    private val url: String = "http://localhost:${AppConfig()["app.port"]}/"

    @Test
    fun `anon user can access homepage`() {
        startApp("auth.allow_anon=true")
        driver.get(url)
        val header = driver.findElement(By.cssSelector("h1"))
        Assertions.assertThat(header.text).isEqualTo("Find a report")

        // anon user should not see logout link, but should see login link
        Assertions.assertThat(driver.findElements(By.className("logout")).count()).isEqualTo(0)
        Assertions.assertThat(driver.findElements(By.className("login")).count()).isEqualTo(1)
    }

    @Test
    fun `anon user can login to escalate privileges`() {
        startApp("auth.allow_anon=true")
        driver.get(url)
        driver.findElement(By.className("login")).findElement(By.cssSelector("a")).click()

        // should be redirected to login page with link
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("login-link")))
        driver.findElement(By.className("login-link")).click()

        // should be directed to login provider, i.e. Montagu in this case
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")))

        driver.findElement(By.name("email")).sendKeys("test.user@example.com")
        driver.findElement(By.name("password")).sendKeys("password")
        driver.findElement(By.id("login-button")).click()

        // should be returned to the site as logged in user
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("site-title")))

        // user should see logout link and should no longer see login link
        Assertions.assertThat(driver.findElements(By.className("login")).count()).isEqualTo(0)
        Assertions.assertThat(driver.findElements(By.className("logout")).count()).isEqualTo(1)
    }
}
