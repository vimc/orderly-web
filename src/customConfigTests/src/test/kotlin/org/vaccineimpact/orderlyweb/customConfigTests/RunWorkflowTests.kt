package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.ExpectedConditions.not
import org.openqa.selenium.support.ui.Select
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGroupGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertUserAndGroup
import org.vaccineimpact.orderlyweb.test_helpers.insertWorkflow


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
        assertThat(tab.findElement(By.tagName("h2")).text).isEqualTo("Run workflow")
    }

    @Test
    fun `can create a blank workflow and select git branch`()
    {
        val tab = driver.findElement(By.id("run-workflow-tab"))
        val page = tab.findElement(By.id("create-workflow-container"))
        val createButton = page.findElement(By.id("create-workflow"))
        assertThat(createButton.isEnabled).isTrue()
        assertThat(createButton.text).isEqualTo("Create a blank workflow")

        createButton.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("git-branch")))
        val branchSelect = driver.findElement(By.id("git-branch"))
        assertThat(branchSelect.getAttribute("value")).isEqualTo("master")
        val commitSelect = driver.findElement(By.id("git-commit"))
        val commitValue = commitSelect.getAttribute("value")
        assertThat(commitValue).isNotBlank()

        //Select a git branch
        Select(branchSelect).selectByIndex(1)
        assertThat(branchSelect.getAttribute("value")).isEqualTo("other")
        //Default commit value should update when new branch selected
        wait.until(not(ExpectedConditions.attributeToBe(commitSelect, "value", commitValue)))
        assertThat(commitSelect.getAttribute("value")).isNotBlank()
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
        assertThat(dropdownMenu[0].text).contains("workflow1\n" +
                "test.user@example.com | Tue Jun 15 2021, 14:50")
        dropdownMenu[0].click()

        val rerunButton = page.findElement(By.id("rerun"))
        assertThat(rerunButton.isEnabled).isTrue()
        assertThat(rerunButton.text).isEqualTo("Re-run workflow")
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
        assertThat(dropdownMenu[0].text).contains("workflow1\n" +
                "test.user@example.com | Tue Jun 15 2021, 14:50")
        dropdownMenu[0].click()

        val cloneButton = page.findElement(By.id("clone"))
        assertThat(cloneButton.isEnabled).isTrue()
        assertThat(cloneButton.text).isEqualTo("Clone workflow")
        cloneButton.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-report-header")))
    }


    @Test
    fun `can add and remove reports from workflow`()
    {
        createWorkflow()
        val nextButton = driver.findElement(By.id("next-workflow"))
        assertThat(nextButton.isEnabled).isFalse()
        addReport("minimal")
        assertThat(driver.findElement(By.cssSelector("#workflow-report-0 label")).text).isEqualTo("minimal")
        assertThat(driver.findElement(By.cssSelector("#workflow-report-0 .text-secondary")).text).isEqualTo("No parameters")
        assertThat(nextButton.isEnabled()).isTrue()

        driver.findElement(By.cssSelector(".remove-report-button")).click()
        assertThat(driver.findElements(By.id("workflow-report-0")).isEmpty()).isTrue()
        assertThat(nextButton.isEnabled()).isFalse()
    }

    @Test
    fun `can set parameter value`()
    {
        createWorkflow()

        //Change branch to find report with parameter
        changeToOtherBranch()

        //Add the report - Next button should be disabled until we set the parameter value
        addReport("other")
        val nextButton = driver.findElement(By.id("next-workflow"))
        assertThat(nextButton.isEnabled).isFalse()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("param-control-0")))
        val parameterInvalidLabel = driver.findElement(By.cssSelector("#workflow-report-0 .text-danger"))
        assertThat(parameterInvalidLabel.text).isEqualTo("Parameter value(s) required")
        driver.findElement(By.id("param-control-0")).sendKeys("1")

        wait.until(ExpectedConditions.textToBe(By.cssSelector("#workflow-report-0 .text-danger"), ""))
        assertThat(nextButton.isEnabled).isTrue()
    }

    @Test
    fun `can change branch and see resulting workflow change`()
    {
        createWorkflow()
        addReport("minimal")

        //Expect report to be removed from workflow when change to a branch where report does not exist
        changeToOtherBranch()
        assertThat(driver.findElements(By.id("workflow-report-0")).isEmpty()).isTrue()
        assertThat(driver.findElement(By.cssSelector(".alert")).text).contains(
                "The following items are not present in this git commit and have been removed from the workflow:\n" +
                "Report 'minimal'")
    }

    @Test
    fun `can progress to finalise step`()
    {
        createWorkflow()
        addReport("minimal")
        val nextButton = driver.findElement(By.id("next-workflow"))
        assertThat(nextButton.isEnabled).isTrue()
        nextButton.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("change-type-control")))

        val submitButton = driver.findElement(By.id("next-workflow"))
        assertThat(submitButton.isEnabled).isFalse()
        val changelogTypes = driver.findElements(By.cssSelector("#change-type-control option"))
        assertThat(changelogTypes.count()).isEqualTo(2)
        assertThat(changelogTypes[0].text).isEqualTo("internal")
        assertThat(changelogTypes[1].text).isEqualTo("public")

        driver.findElement(By.id("run-workflow-name")).sendKeys("new workflow name")
        wait.until(ExpectedConditions.elementToBeClickable(submitButton))
    }

    @Test
    fun `can refresh git`()
    {
        val tab = driver.findElement(By.id("run-workflow-tab"))
        val page = tab.findElement(By.id("create-workflow-container"))
        val createButton = page.findElement(By.id("create-workflow"))

        createButton.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("git-branch")))

        // We generally expect one git commit option in demo orderly before refresh, but have found that this can be two commits on Buildkite run. This may be related to having run a workflow in another test
        assertThat(driver.findElements(By.cssSelector("#git-commit option")).count()).isIn(listOf(1, 2))

        val refreshButton = driver.findElement(By.id("git-refresh-btn"))
        refreshButton.click()
        assertThat(driver.findElements(By.cssSelector(".error-message")).count()).isEqualTo(0)
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("#git-commit option"), 2))
    }

    private fun addReport(reportName: String)
    {
        driver.findElement(By.cssSelector("#workflow-report input")).sendKeys(reportName)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#workflow-report a")))
        driver.findElement(By.cssSelector("#workflow-report a")).click()
        val addButton = driver.findElement(By.id("add-report-button"))
        wait.until(ExpectedConditions.elementToBeClickable(addButton))
        addButton.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("workflow-report-0")))
    }

    private fun createWorkflow()
    {
        val createButton = driver.findElement(By.id("create-workflow"))
        createButton.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("git-branch")))
    }

    private fun changeToOtherBranch()
    {
        val branchSelect = driver.findElement(By.id("git-branch"))
        assertThat(branchSelect.getAttribute("value")).isEqualTo("master")
        val commitSelect = driver.findElement(By.id("git-commit"))
        val commitValue = commitSelect.getAttribute("value")
        assertThat(commitValue).isNotBlank()

        //Select a git branch
        Select(branchSelect).selectByIndex(1)
        wait.until(not(ExpectedConditions.attributeToBe(commitSelect, "value", commitValue)))
        assertThat(branchSelect.getAttribute("value")).isEqualTo("other")
        assertThat(commitSelect.getAttribute("value")).isNotBlank()
    }

    @Test
    fun `can select workflow progress tab and selecting a workflow option generates reports table`()
    {
        //NB This should be replaced with running a workflow through the UI once workflow submit is implemented
        val jse = driver as JavascriptExecutor
        jse.executeScript("""await fetch("${RequestHelper.webBaseUrl}/workflow", {"method": "POST", "body": "{\"name\":\"My workflow\",\"reports\":[{\"name\":\"minimal\", \"params\": {}},{\"name\":\"global\", \"params\": {}}],\"changelog\":{\"message\":\"message1\",\"type\":\"internal\"}}"});""")
        val link = driver.findElement(By.id("workflow-progress-link"))
        assertThat(link.text).isEqualTo("Workflow progress")
        link.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("workflow-progress-tab")))
        val vSelectInput = driver.findElement(By.tagName("input"))
        vSelectInput.sendKeys("My work")
        val vSelect = driver.findElement(By.id("workflows"))
        val dropdownMenu = vSelect.findElements(By.tagName("li"))
        assertThat(dropdownMenu[0].text).contains("My workflow")
        dropdownMenu[0].click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("workflow-table")))
        val table = driver.findElement(By.id("workflow-table"))
        assertThat(table.text).contains("Reports")
        val rows = driver.findElements(By.cssSelector("#workflow-table tr"))
        assertThat(rows.count()).isEqualTo(2)
        val minimalRow = rows.find{ it.text.startsWith("minimal") }!!
        assertThat(minimalRow.text).isIn(listOf("minimal Queued", "minimal Running"))
        val globalRow = rows.find{ it.text.startsWith("global") }!!
        assertThat(globalRow.text).isIn(listOf("global Queued", "global Running"))

        wait.until(ExpectedConditions.textToBePresentInElement(minimalRow,"minimal Complete"))
        wait.until(ExpectedConditions.textToBePresentInElement(globalRow,"global Complete"))
    }

}
