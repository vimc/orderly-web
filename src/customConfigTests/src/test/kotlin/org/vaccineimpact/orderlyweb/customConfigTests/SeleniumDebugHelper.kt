package org.vaccineimpact.orderlyweb.customConfigTests

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestWatcher
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.logging.LogType

class SeleniumDebugHelper: TestWatcher
{
    lateinit var driver: WebDriver

    override fun testFailed(context: ExtensionContext, cause: Throwable)
    {
        val driver = (context.testInstance.get() as SeleniumTest).driver
        System.err.println(driver.findElement(By.cssSelector("body")).getAttribute("innerHTML"))
        System.err.println(driver.manage().logs().get(LogType.BROWSER).joinToString(",") { it.message })
        driver.quit()
    }

    override fun testSuccessful(context: ExtensionContext)
    {
        val driver = (context.testInstance.get() as SeleniumTest).driver
        driver.quit()
    }

}

class DebugHelper: TestWatcher
{
    override fun testFailed(context: ExtensionContext, cause: Throwable)
    {
        cause.printStackTrace()
    }
}
