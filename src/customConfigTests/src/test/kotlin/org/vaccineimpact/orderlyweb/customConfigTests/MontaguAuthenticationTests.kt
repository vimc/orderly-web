package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.vaccineimpact.orderlyweb.db.AppConfig

class MontaguAuthenticationTests : SeleniumTest()
{
    @Test
    fun `user is directed to login with Montagu`()
    {
        startApp("auth.provider=montagu")

        loginWithMontagu()

        val header = driver.findElement(By.cssSelector("h1"))
        assertThat(header.text).isEqualTo("All reports")
    }

    @Test
    fun `user can logout`()
    {
        startApp("auth.provider=montagu")

        loginWithMontagu()

        //Confirm that montagu tokens are set
        val loggedInCookies = driver.manage().cookies
        assertThat(loggedInCookies.first{ it.name == "jwt_token" }.value).isNotEmpty()
        assertThat(loggedInCookies.first{ it.name == "montagu_jwt_token" }.value).isNotEmpty()

        driver.findElement(By.className("logout")).findElement(By.cssSelector("a")).click()
        driver.get(RequestHelper.webBaseUrl)

        assertThat(driver.currentUrl).contains(AppConfig()["montagu.url"])

        //Check that montagu token cookies were cleared to logout of Montagu as well as OrderlyWeb
        val loggedOutCookies = driver.manage().cookies
        assertThat(loggedOutCookies.first{ it.name == "jwt_token" }.value).isEmpty()
        assertThat(loggedOutCookies.first{ it.name == "montagu_jwt_token" }.value).isEmpty()

        //We should now be on the Montagu login page, logged out - give the proxy time to check user status with the api
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("login-button")))

    }
}