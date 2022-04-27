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
    fun `can view run tab, branches, and commits`()
    {
        val tab = driver.findElement(By.id("run-tab"))

        assertThat(tab.findElement(By.tagName("h2")).text).isEqualTo("Run a report")
        val selectBranch = Select(tab.findElement(By.tagName("select")))
        assertThat(selectBranch.firstSelectedOption.text).isEqualTo("master")

        val gitBranch = Select(driver.findElement(By.id("git-branch")))
        assertThat(gitBranch.options.size).isEqualTo(2)
        assertThat(gitBranch.options.map { it.text })
                .containsExactly("master", "other")

        val commitsSelect = Select(driver.findElement(By.id("git-commit")))
        assertThat(commitsSelect.options.size).isEqualTo(2)
        assertThat(commitsSelect.options).allMatch { it.text.contains(Regex("[0-9a-f]{7}")) }
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
    fun `can change git branch to non-master and see different git commits`()
    {
        val gitBranch = driver.findElement(By.id("git-branch"))
        val gitCommit = driver.findElement(By.id("git-commit"))
        val oldCommit = gitCommit.getAttribute("value")

        Select(gitBranch).selectByIndex(1)
        wait.until(ExpectedConditions.not(ExpectedConditions.attributeToBe(gitCommit, "value", oldCommit)))
        assertThat(gitBranch.getAttribute("value")).isEqualTo("other")
        val commitValue = gitCommit.getAttribute("value")
        assertThat(commitValue).isNotBlank()

        // can switch to logs tab and back and retain non-master branch
        driver.findElement(By.id("logs-link")).click()
        val logsTab = driver.findElement(By.id("logs-tab"))
        wait.until(ExpectedConditions.attributeToBe(logsTab, "display", "block"))

        driver.findElement(By.id("run-link")).click()
        val runTab = driver.findElement(By.id("run-tab"))
        wait.until(ExpectedConditions.attributeToBe(runTab, "display", "block"))

        val gitBranch2 = driver.findElement(By.id("git-branch"))
        assertThat(gitBranch2.getAttribute("value")).isEqualTo("other")
    }

}
