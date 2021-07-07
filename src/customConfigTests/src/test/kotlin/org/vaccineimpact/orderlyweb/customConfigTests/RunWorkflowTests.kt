package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.ExpectedConditions.not
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGroupGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertUserAndGroup
import org.vaccineimpact.orderlyweb.test_helpers.insertWorkflow
import java.time.Duration


class RunWorkflowTests : SeleniumTest()
{
    @Before
    fun setUp()
    {
        JooqContext().use {
            insertUserAndGroup(it, "test.user@example.com")
            insertWorkflow("test.user@example.com", "newkey", "workflow1")
            giveUserGroupGlobalPermission(it, "test.user@example.com", "reports.run")
        }

        startApp("auth.provider=montagu")

        loginWithMontagu()

        val url = RequestHelper.webBaseUrl + "/run-workflow/"
        driver.get(url)
    }

    @Test
    fun `can view run workflow tab`()
    {
        val tab = driver.findElement(By.id("run-workflow-tab"))
        Assertions.assertThat(tab.findElement(By.tagName("h2")).text).isEqualTo("Run workflow")
    }

    @Test
    fun `can create a blank workflow`()
    {
        val tab = driver.findElement(By.id("run-workflow-tab"))
        val page = tab.findElement(By.id("create-workflow-container"))
        val createButton = page.findElement(By.id("create-workflow"))
        Assertions.assertThat(createButton.isEnabled).isTrue()
        Assertions.assertThat(createButton.text).isEqualTo("Create a blank workflow")
        createButton.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("git-branch")))
        val branchSelect = driver.findElement(By.id("git-branch"))
        Assertions.assertThat(branchSelect.getAttribute("value")).isEqualTo("master")
        val commitSelect = driver.findElement(By.id("git-commit"))
        val commitValue = commitSelect.getAttribute("value")
        Assertions.assertThat(commitValue).isNotBlank()

        //Select a git branch
        Select(branchSelect).selectByIndex(1)
        Assertions.assertThat(branchSelect.getAttribute("value")).isEqualTo("other")
        //Default commit value should update when new branch selected
        wait.until(not(ExpectedConditions.attributeToBe(commitSelect, "value", commitValue)))
        Assertions.assertThat(commitSelect.getAttribute("value")).isNotBlank()
    }

    @Test
    fun `can rerun workflow`()
    {
        val tab = driver.findElement(By.id("run-workflow-tab"))
        val page = tab.findElement(By.id("create-workflow-container"))
        val vSelectInput = driver.findElement(By.tagName("input"))
        vSelectInput.sendKeys("workf")

        val vSelect = driver.findElement(By.id("v-select"))
        val dropdownMenu = vSelect.findElements(By.tagName("li"))
        Assertions.assertThat(dropdownMenu[0].text).contains("workflow1\n" +
                        "test.user@example.com | Tue Jun 15 2021, 14:50")
        dropdownMenu[0].click()

        val rerunButton = page.findElement(By.id("rerun"))
        Assertions.assertThat(rerunButton.isEnabled).isTrue()
        Assertions.assertThat(rerunButton.text).isEqualTo("Re-run workflow")
        rerunButton.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("run-header")))
    }

    @Test
    fun `can clone workflow`()
    {
        val tab = driver.findElement(By.id("run-workflow-tab"))
        val page = tab.findElement(By.id("create-workflow-container"))
        val vSelectInput = driver.findElement(By.tagName("input"))
        vSelectInput.sendKeys("workf")

        val vSelect = driver.findElement(By.id("v-select"))
        val dropdownMenu = vSelect.findElements(By.tagName("li"))
        Assertions.assertThat(dropdownMenu[0].text).contains("workflow1\n" +
                "test.user@example.com | Tue Jun 15 2021, 14:50")
        dropdownMenu[0].click()

        val cloneButton = page.findElement(By.id("clone"))
        Assertions.assertThat(cloneButton.isEnabled).isTrue()
        Assertions.assertThat(cloneButton.text).isEqualTo("Clone workflow")
        cloneButton.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-report-header")))
    }

    @Test
    fun `can refresh git`()
    {
        val tab = driver.findElement(By.id("run-workflow-tab"))
        val page = tab.findElement(By.id("create-workflow-container"))
        val createButton = page.findElement(By.id("create-workflow"))

        createButton.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("git-branch")))

        Assertions.assertThat(driver.findElements(By.cssSelector("#git-commit option")).count()).isEqualTo(1)

        val refreshButton = driver.findElement(By.id("git-refresh-btn"))
        refreshButton.click()
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("#git-commit option"), 2))
    }
}
