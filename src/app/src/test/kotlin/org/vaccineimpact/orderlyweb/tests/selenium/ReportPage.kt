package org.vaccineimpact.orderlyweb.tests.selenium
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy

class ReportPage(driver: WebDriver): SeleniumPage(driver) {

    @FindBy(className = "toggle")
    lateinit var publishButton: WebElement

    override fun open() {
        // go to main reports page
        driver.get("http://localhost:8081/reports")
        // click on first report
        driver.findElements(By.ByCssSelector("td a")).first().click()
    }

    fun togglePublish() {
        publishButton.click()
    }

}