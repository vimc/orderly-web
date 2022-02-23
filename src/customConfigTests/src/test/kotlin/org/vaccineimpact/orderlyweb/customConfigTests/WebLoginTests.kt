package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions
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
    fun `can login with gitHub`()
    {
        val gitLogin = driver.findElement(By.cssSelector(".login-link"))
        Assertions.assertThat(gitLogin.text).isEqualToIgnoringWhitespace("Log in with GitHub")

        gitLogin.click()
        wait.until(ExpectedConditions.urlContains("https://github.com/"))
    }
}