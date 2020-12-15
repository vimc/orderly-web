package org.vaccineimpact.orderlyweb.customConfigTests

import org.junit.Test
import org.junit.Before
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.assertj.core.api.Assertions.assertThat
import org.openqa.selenium.support.ui.Select
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGroupGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertUserAndGroup

class RunReportPageTests : SeleniumTest()
{
    @Before
    fun setUp()
    {
        JooqContext().use {
            insertUserAndGroup(it, "test.user@example.com")
            giveUserGroupGlobalPermission(it, "test.user@example.com", "reports.run")
        }

        startApp("auth.provider=montagu")

        loginWithMontagu()

        val url = RequestHelper.webBaseUrl + "/run-report/"
        driver.get(url)
    }

    @Test
    fun `can view run tab`()
    {
        val tab = driver.findElement(By.id("run-tab"))

        assertThat(tab.findElement(By.tagName("h2")).text).isEqualTo("Run a report")
        val select = Select(tab.findElement(By.tagName("select")))
        assertThat(select.firstSelectedOption.text).isEqualTo("master")
    }

    @Test
    fun `can view git commits`()
    {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("git-commit")))
        val commitsSelect = Select(driver.findElement(By.id("git-commit")))
        assertThat(commitsSelect.options.size).isEqualTo(2)
    }

    @Test
    fun `can view logs tab`()
    {
        driver.findElement(By.id("logs-link")).click()

        val tab = driver.findElement(By.id("logs-tab"))
        wait.until(ExpectedConditions.attributeToBe(tab,"display", "block"))

        assertThat(tab.findElement(By.tagName("h2")).text).isEqualTo("Report logs")
        assertThat(tab.findElement(By.tagName("p")).text).isEqualTo("Report logs coming soon!")
    }
}
