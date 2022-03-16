package org.vaccineimpact.orderlyweb.customConfigTests

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.logging.LogType

class DebugHelper: TestWatcher()
{
    lateinit var driver: WebDriver

    override fun failed(e: Throwable, description: Description)
    {
        System.err.println(driver.findElement(By.cssSelector("body")).getAttribute("innerHTML"))
    }

    override fun finished(description: Description)
    {
        System.err.println(driver.manage().logs().get(LogType.BROWSER).joinToString(",") { it.message })
        driver.quit()
    }
}
