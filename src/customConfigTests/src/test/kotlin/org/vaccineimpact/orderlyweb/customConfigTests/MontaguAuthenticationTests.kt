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

        driver.findElement(By.className("logout")).findElement(By.cssSelector("a")).click()
        driver.get(RequestHelper.webBaseUrl)

        assertThat(driver.currentUrl).contains(AppConfig()["montagu.url"])
    }
}