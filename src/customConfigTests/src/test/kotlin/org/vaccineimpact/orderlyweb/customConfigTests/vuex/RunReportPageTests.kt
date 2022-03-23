package org.vaccineimpact.orderlyweb.customConfigTests.vuex

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.vaccineimpact.orderlyweb.customConfigTests.RequestHelper
import org.vaccineimpact.orderlyweb.customConfigTests.SeleniumTest
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGroupGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
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

        val url = RequestHelper.webBaseUrl + "/vuex-run-report/"
        driver.get(url)
    }

    @Test
    fun `can view run tab`()
    {
        val tab = driver.findElement(By.id("run-tab"))

        assertThat(tab.findElement(By.tagName("h2")).text).isEqualTo("Run a report")
    }

    @Test
    fun `can view logs tab`()
    {
        driver.findElement(By.id("logs-link")).click()

        val tab = driver.findElement(By.id("logs-tab"))
        wait.until(ExpectedConditions.attributeToBe(tab, "display", "block"))
        assertThat(tab.findElement(By.tagName("h2")).text).isEqualTo("Running report logs")
    }

    @Test
    fun `can view git branches`()
    {
        val gitBranch = Select(driver.findElement(By.id("git-branch")))
        assertThat(gitBranch.options.size).isEqualTo(2)
        assertThat(gitBranch.options.map { it.text })
                .containsExactly("master", "other")
    }

}
