package org.vaccineimpact.orderlyweb.customConfigTests

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.logging.LogType

class SeleniumDebugHelper: TestWatcher()
{
    lateinit var driver: WebDriver

    override fun failed(e: Throwable, description: Description)
    {
        System.err.println(driver.findElement(By.cssSelector("body")).getAttribute("innerHTML"))
        System.err.println(driver.manage().logs().get(LogType.BROWSER).joinToString(",") { it.message })
    }

    override fun finished(description: Description)
    {
        driver.quit()
    }
}

class DebugHelper: TestWatcher()
{
    override fun failed(e: Throwable, description: Description)
    {
        e.printStackTrace()
    }
}
