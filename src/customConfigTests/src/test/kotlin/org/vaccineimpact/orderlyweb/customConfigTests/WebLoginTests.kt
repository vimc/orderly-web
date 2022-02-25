package org.vaccineimpact.orderlyweb.customConfigTests

import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions

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
    fun `can trigger login with gitHub link`()
    {
        val authProvider = driver.findElement(By.cssSelector(".login-link"))

        authProvider.click()
        wait.until(ExpectedConditions.urlContains("https://github.com/login"))
    }
}