package org.vaccineimpact.orderlyweb.customConfigTests

import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.openqa.selenium.support.ui.Select
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGroupGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertUserAndGroup

class RunReportPageTests : SeleniumTest()
{
    @BeforeEach
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
    fun `can view run tab with querystring`()
    {
        val url = RequestHelper.webBaseUrl + "/run-report?report-name=minimal"
        driver.get(url)

        val tab = driver.findElement(By.id("run-tab"))
        assertThat(tab.findElement(By.tagName("h2")).text).isEqualTo("Run a report")

        val typeahead = driver.findElement(By.id("report"))
        val matches = typeahead.findElements(By.cssSelector(".vs__selected-options span"))
        assertThat(matches.size).isEqualTo(1)
    }

    @ParameterizedTest
    @EnumSource(ConfigType::class)
    fun `can view git commits only when git allowed`(configType: ConfigType)
    {
        if (configType == ConfigType.GIT_ALLOWED)
        {
            val tab = driver.findElement(By.id("run-tab"))
            val selectBranch = Select(tab.findElement(By.tagName("select")))
            assertThat(selectBranch.firstSelectedOption.text).isEqualTo("master")
            val commitsSelect = Select(driver.findElement(By.id("git-commit")))
            assertThat(commitsSelect.options.size).isEqualTo(1)
            assertThat(commitsSelect.options).allMatch { it.text.contains(Regex("[0-9a-f]{7}")) }
        }
        else
        {
            assertThat(driver.findElements(By.id("git-commit"))).isEmpty()
        }
    }

    @Test
    fun `can view and select reports`()
    {
        val typeahead = driver.findElement(By.id("report"))
        typeahead.click()
        assertThat(typeahead.findElements(By.tagName("li")).size).isEqualTo(2)
        val input = typeahead.findElement(By.tagName("input"))
        input.sendKeys("min")
        val matches = typeahead.findElements(By.tagName("li"))
        assertThat(matches.size).isEqualTo(1)
        assertThat(matches[0].text).startsWith("minimal")
    }

    @Test
    fun `can change git branch to non-master and see changed reports only`()
    {
        val gitBranch = driver.findElement(By.id("git-branch"))
        val gitCommit = driver.findElement(By.id("git-commit"))
        val oldCommit = gitCommit.getAttribute("value")

        Select(gitBranch).selectByIndex(1)
        wait.until(ExpectedConditions.not(ExpectedConditions.attributeToBe(gitCommit, "value", oldCommit)))
        assertThat(gitBranch.getAttribute("value")).isEqualTo("other")
        val commitValue = gitCommit.getAttribute("value")
        assertThat(commitValue).isNotBlank()

        // 'minimal' and 'global' reports from master should not be included, 'other' and 'view' should be
        assertThat(reportIsAvailable("minimal")).isFalse()
        assertThat(reportIsAvailable("global")).isFalse()
        assertThat(reportIsAvailable("other")).isTrue()
        assertThat(reportIsAvailable("view")).isTrue()
    }

    @Test
    fun `can run a report and follow view log link to logs`()
    {
        val typeahead = driver.findElement(By.id("report"))
        val input = typeahead.findElement(By.tagName("input"))
        input.sendKeys("min")
        val matches = typeahead.findElements(By.tagName("li"))
        matches[0].click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("run-form-group")))

        val button = driver.findElement(By.cssSelector("#run-form-group button"))
        button.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("run-report-status")))
        assertThat(driver.findElement(By.id("run-report-status")).text).startsWith("Run started")
        assertThat(driver.findElement(By.cssSelector("#run-report-status a")).text).startsWith("View log")

        driver.findElement(By.cssSelector("#run-report-status a")).click()
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h2"), "Running report logs"))
    }

    @Test
    fun `can run a report and view logs`()
    {
        val typeahead = driver.findElement(By.id("report"))
        val input = typeahead.findElement(By.tagName("input"))
        input.sendKeys("min")
        val matches = typeahead.findElements(By.tagName("li"))
        matches[0].click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("run-form-group")))

        val button = driver.findElement(By.cssSelector("#run-form-group button"))
        button.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("run-report-status")))
        assertThat(driver.findElement(By.id("run-report-status")).text).startsWith("Run started")

        driver.findElement(By.id("logs-link")).click()
        wait.until(ExpectedConditions.textToBe(By.cssSelector("#report-status .font-weight-bold"), "running"))
        // Ensure that expected report is selected in dropdown
        assertThat(driver.findElement(By.cssSelector("#logs")).text).startsWith("minimal")
    }

    @Test
    fun `can view logs tab`()
    {
        driver.findElement(By.id("logs-link")).click()

        val tab = driver.findElement(By.id("logs-tab"))
        wait.until(ExpectedConditions.attributeToBe(tab, "display", "block"))
        assertThat(tab.findElement(By.tagName("h2")).text).isEqualTo("Running report logs")
        assertThat(tab.findElement(By.tagName("p")).text).isEqualTo("No reports have been run yet")
    }

    //TODO: This test case should be revisited as soon as test data is updated.
    @Test
    @Disabled
    fun `can fill parameter textField`()
    {
        val gitBranch = Select(driver.findElement(By.id("git-branch")))
        gitBranch.selectByVisibleText("other")

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("report")))
        val typeahead = driver.findElement(By.id("report"))
        val input = typeahead.findElement(By.tagName("input"))
        input.sendKeys("other")
        val matches = typeahead.findElements(By.tagName("li"))
        matches[0].click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("parameters")))
        val parameters = driver.findElement(By.id("params-component"))
        val paramInput = parameters.findElements(By.tagName("input"))

        paramInput[0].clear()
        paramInput[0].sendKeys("new value")
        assertThat(paramInput[0].text).isEqualTo("new value")
    }

    @Test
    fun `can add change message and type values`()
    {
        val typeahead = driver.findElement(By.id("report"))
        typeahead.click()
        assertThat(typeahead.findElements(By.tagName("li")).size).isEqualTo(2)
        val input = typeahead.findElement(By.tagName("input"))
        input.sendKeys("min")
        val matches = typeahead.findElements(By.tagName("li"))
        matches[0].click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("changelog-message")))
        val changeLog = driver.findElement(By.id("changelog-message"))
        val textarea = changeLog.findElement(By.className("form-control"))
        textarea.sendKeys("New text message")
        assertThat(textarea.getAttribute("value")).isEqualTo("New text message")

        val changelogType = Select(driver.findElement(By.id("changelogType")))
        changelogType.selectByValue("public")
        assertThat(changelogType.firstSelectedOption.getAttribute("value")).isEqualTo("public")
    }

    private fun reportIsAvailable(reportName: String): Boolean
    {
        val typeahead = driver.findElement(By.id("report"))
        val input = typeahead.findElement(By.tagName("input"))
        input.clear()
        input.sendKeys(reportName)

        val listItem = typeahead.findElements(By.tagName("li"))[0]
        val foundReportName = if (listItem.getAttribute("role") == "option")
        {
            listItem.findElements(By.tagName("span"))[0].getAttribute("innerHTML")
        }
        else
        {
            null
        }
        return foundReportName == reportName
    }
}
