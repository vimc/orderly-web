package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.vaccineimpact.orderlyweb.db.AppConfig

class WebLoginTests: SeleniumTest()
{

    @Before
    fun setUp()
    {
        startApp("auth.provider=github")

        val url = RequestHelper.webBaseUrl
        driver.get(url)
    }

    @Test
    fun `can login with gitHub`()
    {
        val authProvider = driver.findElement(By.cssSelector(".login-link"))
        Assertions.assertThat(authProvider.text).isEqualToIgnoringWhitespace("Log in with GitHub")

        authProvider.click()
        wait.until(ExpectedConditions.urlContains("https://github.com/login"))
    }
}