package org.vaccineimpact.orderlyweb.customConfigTests

import org.junit.After
import org.junit.Before
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.WebDriverWait

abstract class SeleniumTest : CustomConfigTests()
{
    protected lateinit var driver: WebDriver

    protected lateinit var wait: WebDriverWait

    @Before
    fun setup()
    {
        driver = ChromeDriver(org.openqa.selenium.chrome.ChromeOptions()
                .apply { addArguments("--ignore-certificate-errors", "--headless")  })
        wait = WebDriverWait(driver, 10)
    }

    @After
    fun tearDown()
    {
        driver.quit()
    }

}
