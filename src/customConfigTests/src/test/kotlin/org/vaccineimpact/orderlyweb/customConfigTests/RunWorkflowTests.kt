package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGroupGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertUserAndGroup
import org.vaccineimpact.orderlyweb.test_helpers.insertWorkflow
// import org.vaccineimpact.orderlyweb.test_helpers.insertWorkflowWithReports

class RunWorkflowTests : SeleniumTest()
{
    @Before
    fun setUp()
    {
        JooqContext().use {
            insertUserAndGroup(it, "test.user@example.com")
            insertWorkflow("test.user@example.com", "newkey", "workflow1")
            // insertWorkflowWithReports("test.user@example.com", "newkey", "workflow1")
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

    // @Test
    // fun `can select workflow progress tab and selecting a workflow option generates reports table`()
    // {
    //     val link = driver.findElement(By.id("workflow-progress-link"))
    //     Assertions.assertThat(link.text).isEqualTo("Workflow progress")
    //     link.click()
    //     wait.until(ExpectedConditions.presenceOfElementLocated(By.id("workflow-progress-tab")))
    //     val vSelectInput = driver.findElement(By.tagName("input"))
    //     vSelectInput.sendKeys("workf")

    //     val vSelect = driver.findElement(By.id("v-select"))
    //     val dropdownMenu = vSelect.findElements(By.tagName("li"))
    //     Assertions.assertThat(dropdownMenu[0].text).contains("workflow1\n" +
    //         "Tue Jun 15 2021, 14:50")
    //     dropdownMenu[0].click()
    //     wait.until(ExpectedConditions.presenceOfElementLocated(By.id("workflow-table")))
    //     val table = driver.findElement(By.id("workflow-table"))
    //     Assertions.assertThat(table.text).contains("Reports")
    // }

    fun `can create a blank workflow`()
    {
        val tab = driver.findElement(By.id("run-workflow-tab"))
        val page = tab.findElement(By.id("create-workflow-container"))
        val createButton = page.findElement(By.id("create-workflow"))
        Assertions.assertThat(createButton.isEnabled).isTrue()
        Assertions.assertThat(createButton.text).isEqualTo("Create a blank workflow")
        createButton.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-report-header")))
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
}
