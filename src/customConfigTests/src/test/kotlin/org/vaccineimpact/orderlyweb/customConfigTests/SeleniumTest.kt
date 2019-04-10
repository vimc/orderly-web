package org.vaccineimpact.orderlyweb.customConfigTests

import org.junit.After
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

abstract class SeleniumTest : CustomConfigTests()
{
    protected var driver: WebDriver = ChromeDriver(ChromeOptions()
            .apply {  })
//addArguments("--headless")
    @After
    fun tearDown()
    {
      //  driver.quit()
    }
}
