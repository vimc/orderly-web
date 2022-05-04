package org.vaccineimpact.orderlyweb.customConfigTests

import java.nio.file.Files
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
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

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".workflow-options")))
        val vSelect = driver.findElement(By.id("v-select"))
        val dropdownMenu = vSelect.findElements(By.tagName("li"))
        assertThat(dropdownMenu[0].text).contains("workflow1 test.user@example.com | Tue Jun 15 2021, 14:50")
        dropdownMenu[0].click()

        val rerunButton = page.findElement(By.id("rerun"))
        assertThat(rerunButton.isEnabled).isTrue()
        assertThat(rerunButton.text).isEqualTo("Re-run workflow")
        rerunButton.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("summary-header")))
    }

    @Test
    fun `can clone workflow`()
    {
        val tab = driver.findElement(By.id("run-workflow-tab"))
        val page = tab.findElement(By.id("create-workflow-container"))
        val vSelectInput = driver.findElement(By.tagName("input"))
        vSelectInput.sendKeys("workf")

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".workflow-options")))
        val vSelect = driver.findElement(By.id("v-select"))
        val dropdownMenu = vSelect.findElements(By.tagName("li"))
        assertThat(dropdownMenu[0].text).contains("workflow1 test.user@example.com | Tue Jun 15 2021, 14:50")
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
        assertThat(nextButton.isEnabled).isTrue()

        driver.findElement(By.cssSelector(".remove-report-button")).click()
        assertThat(driver.findElements(By.id("workflow-report-0")).isEmpty()).isTrue()
        assertThat(nextButton.isEnabled).isFalse()
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
        changeToOtherBranch()
        addReport("other")

        //Expect report to be removed from workflow when change to a branch where report does not exist
        changeToMasterBranch()
        assertThat(driver.findElements(By.id("workflow-report-0")).isEmpty()).isTrue()
        assertThat(driver.findElement(By.cssSelector(".alert")).text).contains(
                "The following items are not present in this git commit and have been removed from the workflow:\n" +
                "Report 'other'")
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

    @Test
    fun `can import csv file`()
    {
        val tmpFile = Files.createTempFile("test_import", ".csv").toFile()
        tmpFile.writeText("report\nminimal")

        createWorkflow()
        driver.findElement(By.id("import-from-csv-label")).click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("import-csv")))

        val fileInput = driver.findElement(By.id("import-csv"))
        fileInput.sendKeys(tmpFile.absolutePath)

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("workflow-report-0")))

        tmpFile.delete()
    }

    @Test
    fun `can see all reports in non-master branch`()
    {
        // Available reports should include those which are unchanged from master branch i.e. 'minimal' and 'global', as
        // well as branch-only reports e.g. 'other'
        createWorkflow()
        changeToOtherBranch()
        addReport("minimal")
        addReport("global")
        addReport("other")
    }

    private fun addReport(reportName: String)
    {
        driver.findElement(By.cssSelector("#workflow-report input")).sendKeys(reportName)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#workflow-report li")))
        driver.findElement(By.cssSelector("#workflow-report li")).click()
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

    private fun changeToMasterBranch()
    {
        changeToBranch(0, "master", "other")
    }

    private fun changeToOtherBranch()
    {
        changeToBranch(1, "other", "master")
    }

    private fun changeToBranch(newBranchIndex: Int, newBranch: String, expectedCurrentBranch: String)
    {
        val branchSelect = driver.findElement(By.id("git-branch"))
        assertThat(branchSelect.getAttribute("value")).isEqualTo(expectedCurrentBranch)
        val commitSelect = driver.findElement(By.id("git-commit"))
        val commitValue = commitSelect.getAttribute("value")
        assertThat(commitValue).isNotBlank()

        //Select a git branch
        Select(branchSelect).selectByIndex(newBranchIndex)
        wait.until(not(ExpectedConditions.attributeToBe(commitSelect, "value", commitValue)))
        assertThat(branchSelect.getAttribute("value")).isEqualTo(newBranch)
        assertThat(commitSelect.getAttribute("value")).isNotBlank()
    }

    @Test
    fun `can create a workflow and select the view progress link to navigate to the progress tab with workflow preselected and reports table generated, which persists when navigating off tab and back again, and re-run workflow in progress`()
    {
        // creates workflow with ui and navigates to the progress page with it selected
        createWorkflow()
        addReport("minimal")
        addReport("global")
        var nextButton = driver.findElement(By.id("next-workflow"))
        assertThat(nextButton.isEnabled).isTrue()
        nextButton.click()

        // should now be on summary page
        nextButton = driver.findElement(By.id("next-workflow"))
        assertThat(nextButton.isEnabled).isTrue()
        nextButton.click()

        // should now be on run page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("change-type-control")))
        val submitButton = driver.findElement(By.id("next-workflow"))
        assertThat(submitButton.isEnabled).isFalse()
        val changelogTypes = driver.findElements(By.cssSelector("#change-type-control option"))
        assertThat(changelogTypes.count()).isEqualTo(2)
        assertThat(changelogTypes[0].text).contains("internal")
        assertThat(changelogTypes[1].text).contains("public")
        driver.findElement(By.id("run-workflow-name")).sendKeys("My workflow")
        driver.findElement(By.id("changelogMessage")).sendKeys("changes")
        wait.until(ExpectedConditions.elementToBeClickable(submitButton))
        submitButton.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("view-progress-link")))
        assertThat(driver.findElement(By.id("view-progress-link")).text).isEqualTo("View workflow progress")
        val progressLink = driver.findElement(By.cssSelector("#view-progress-link a"))
        progressLink.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("workflow-progress-tab")))
        val selectedWorkflow = driver.findElement(By.cssSelector(".vs__selected"))
        assertThat(selectedWorkflow.text).contains("My workflow")
        val table = driver.findElement(By.id("workflow-table"))
        assertThat(table.text).contains("Reports")
        val rows = driver.findElements(By.cssSelector("#workflow-table tr"))
        assertThat(rows.count()).isEqualTo(3)
        val minimalRow = rows.find{ it.text.startsWith("minimal") }!!
        val minimalRowStatus = minimalRow.findElement(By.cssSelector("td:nth-child(2)"))
        assertThat(minimalRowStatus.text).isIn(listOf("Queued", "Running"))
        val globalRow = rows.find{ it.text.startsWith("global") }!!
        val globalRowStatus = globalRow.findElement(By.cssSelector("td:nth-child(2)"))
        assertThat(globalRowStatus.text).isIn(listOf("Queued", "Running"))
        wait.until(ExpectedConditions.textToBePresentInElement(minimalRowStatus,"Complete"))
        wait.until(ExpectedConditions.textToBePresentInElement(globalRow,"Complete"))

        // view report log
        val viewLogLink = minimalRow.findElement(By.cssSelector("a.report-log-link"))
        viewLogLink.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#report-name")))

        assertThat(driver.findElement(By.cssSelector("#report-name span.font-weight-bold")).text).isEqualTo("minimal")
        assertThat(driver.findElement(By.cssSelector("#report-git-branch span.font-weight-bold")).text).isEqualTo("master")
        assertThat(driver.findElement(By.cssSelector("#report-status span.font-weight-bold")).text).isEqualTo("success")
        assertThat(driver.findElement(By.cssSelector("#report-logs textarea")).getAttribute("value")).startsWith("[ git")

        val closeLogButton = driver.findElement(By.cssSelector("#report-log-dialog button.modal-buttons"))
        closeLogButton.click()

        // navigates away and back to the progress tab and checks workflow is still selected
        val workflowLink = driver.findElement(By.id("run-workflow-link"))
        workflowLink.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("run-workflow-tab")))
        val link = driver.findElement(By.id("workflow-progress-link"))
        assertThat(link.text).isEqualTo("Workflow progress")
        link.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("workflow-progress-tab")))
        val selectedWorkflow2 = driver.findElement(By.cssSelector(".vs__selected"))
        assertThat(selectedWorkflow2.text).contains("My workflow")

        // clicks re-run workflow
        driver.findElement(By.id("rerun")).click()

        // on summary page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("summary-header")))
        driver.findElement(By.id("next-workflow")).click()

        // on run page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("run-header")))
        val workflowNameInput = driver.findElement(By.cssSelector("#workflow-name-div input"))
        assertThat(workflowNameInput.getAttribute("value")).isEqualTo("My workflow")
        assertThat(workflowNameInput.getAttribute("readonly")).isEqualTo("true")
    }

    @Test
    fun `workflow progress link clears when updating the wizard`()
    {
        createWorkflow()
        addReport("minimal")
        addReport("global")
        var nextButton = driver.findElement(By.id("next-workflow"))
        nextButton.click()

        // should now be on summary page
        nextButton = driver.findElement(By.id("next-workflow"))
        nextButton.click()

        // now on run page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("change-type-control")))
        val submitButton = driver.findElement(By.id("next-workflow"))
        driver.findElement(By.id("run-workflow-name")).sendKeys("My workflow")
        driver.findElement(By.id("changelogMessage")).sendKeys("changes")
        wait.until(ExpectedConditions.elementToBeClickable(submitButton))
        submitButton.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("view-progress-link")))
        driver.findElement(By.id("run-workflow-name")).sendKeys("more text")
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("view-progress-link")))
    }

    @Test
    fun `can display workflow summary`()
    {
        createWorkflow()

        //Change branch to find report with parameter
        changeToOtherBranch()

        //Add the report - Next button should be disabled until we set the parameter value
        addReport("other")
        val nextButton = driver.findElement(By.id("next-workflow"))
        assertThat(nextButton.isEnabled).isFalse()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("param-control-0")))
        driver.findElement(By.id("param-control-0")).sendKeys("1")

        addReport("use_dependency_2")

        wait.until(ExpectedConditions.textToBe(By.cssSelector("#workflow-report-0 .text-danger"), ""))
        assertThat(nextButton.isEnabled).isTrue()
        nextButton.click()

        // now on summary page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("summary-header")))

        assertThat(driver.findElement(By.cssSelector("#summary-warning .d-inline-block")).text).isEqualTo("Some reports depend on the latest version of other reports that are not included in your workflow:")
        assertThat(driver.findElements(By.cssSelector("#summary-warning .font-weight-bold")).count()).isEqualTo(1)
        assertThat(driver.findElement(By.cssSelector("#summary-warning .font-weight-bold")).text).isEqualTo("use_dependency_2")
        assertThat(driver.findElements(By.cssSelector("#summary-warning li")).count()).isEqualTo(1)
        assertThat(driver.findElement(By.cssSelector("#summary-warning li")).text).isEqualTo("use_dependency")

        val summaryReportNameDivs = driver.findElements(By.id("report-name-icon"))
        assertThat(summaryReportNameDivs.count()).isEqualTo(2)
        assertThat(summaryReportNameDivs[0].text).isEqualTo("other")
        assertThat(summaryReportNameDivs[1].text).isEqualTo("use_dependency_2")

        val parameterHeading = driver.findElement(By.cssSelector(".report-params span"))
        assertThat(parameterHeading.text).isEqualTo("Parameters")

        val params = driver.findElements(By.className("non-default-param"))
        assertThat(params.count()).isEqualTo(1)
        assertThat(params[0].text).contains("nmin: 1")

        val noParams = driver.findElements(By.cssSelector(".no-params"))
        assertThat(noParams.count()).isEqualTo(1)
        assertThat(noParams[0].text).isEqualTo("No parameters")

        assertThat(driver.findElements(By.cssSelector(".error-message")).count()).isEqualTo(0)

        // go back to previous page and remove second report

        val backButton = driver.findElement(By.id("previous-workflow"))
        backButton.click()
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("workflow-report-1")))
        val removeReportBtns = driver.findElements(By.cssSelector(".remove-report-button"))
        removeReportBtns[1].click()

        // return to summary page and see that missing dependencies warnings are no longer in the summary header

        val nextButton2 = driver.findElement(By.id("next-workflow"))
        nextButton2.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("summary-header")))
        assertThat(driver.findElements(By.id("summary-warning")).count()).isEqualTo(0)
    }

    @Test
    fun `can display workflow summary and progress with default params, non-default params, and no params`()
    {
        createWorkflow()
        changeToOtherBranch()
        addReport("default-param")
        addReport("global")
        addReport("other")

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#workflow-report-2 #param-control-0")))
        driver.findElement(By.cssSelector("#workflow-report-2 #param-control-0")).sendKeys("1")

        var nextButton = driver.findElement(By.id("next-workflow"))
        wait.until(ExpectedConditions.textToBe(By.cssSelector("#workflow-report-2 .text-danger"), ""))
        assertThat(nextButton.isEnabled).isTrue()
        nextButton.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("show-defaults")))
        val summaryReportNameDiv = driver.findElement(By.id("report-name-icon"))
        assertThat(summaryReportNameDiv.text).isEqualTo("default-param")

        val parameterHeading = driver.findElement(By.cssSelector(".report-params span"))
        assertThat(parameterHeading.text).isEqualTo("Parameters")

        val params = driver.findElements(By.className("report-params"))
        assertThat(params.count()).isEqualTo(3)

        var nonDefaultParams = params[0].findElements(By.className("non-default-param"))
        assertThat(nonDefaultParams).isEmpty()

        var defaultParams = params[0].findElement(By.id("default-params-0"))
        var collapsedParams = defaultParams.findElement(By.id("collapseSummary-0"))
        assertThat(collapsedParams.isDisplayed).isFalse()

        var showDefault = defaultParams.findElement(By.cssSelector("a"))
        assertThat(showDefault.text).isEqualTo("Show defaults...")
        showDefault.click()

        wait.until(ExpectedConditions.visibilityOf(collapsedParams))
        assertThat(showDefault.text).isEqualTo("Hide defaults...")

        assertThat(defaultParams.findElement(By.id("default-params-collapse-0-0")).getAttribute("innerHTML")).isEqualTo("disease: HepB")
        assertThat(defaultParams.findElement(By.id("default-params-collapse-0-1")).getAttribute("innerHTML")).isEqualTo("nmin: 0.5")

        showDefault.click()
        wait.until(ExpectedConditions.invisibilityOf(collapsedParams))
        assertThat(showDefault.text).isEqualTo("Show defaults...")

        nextButton = driver.findElement(By.id("next-workflow"))
        assertThat(nextButton.isEnabled).isTrue()
        nextButton.click()

        // should now be on run page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("change-type-control")))
        val submitButton = driver.findElement(By.id("next-workflow"))
        assertThat(submitButton.isEnabled).isFalse()
        driver.findElement(By.id("run-workflow-name")).sendKeys("My workflow")
        driver.findElement(By.id("changelogMessage")).sendKeys("changes")
        wait.until(ExpectedConditions.elementToBeClickable(submitButton))
        submitButton.click()

        // should now be on progress page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("view-progress-link")))
        val progressLink = driver.findElement(By.cssSelector("#view-progress-link a"))
        progressLink.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("workflow-progress-tab")))
        val selectedWorkflow = driver.findElement(By.cssSelector(".vs__selected"))
        assertThat(selectedWorkflow.text).contains("My workflow")

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("table-params-header")))
        val tableHeader = driver.findElement(By.id("table-params-header"))
        assertThat(tableHeader.text).isEqualTo("Parameters")
        val tableParams = driver.findElements(By.className("tableParams"))
        assertThat(tableParams.count()).isEqualTo(3)

        // the table rows can appear in any order so we have to be general with our queries

        nonDefaultParams = driver.findElements(By.className("non-default-param"))
        assertThat(nonDefaultParams.count()).isEqualTo(1)
        assertThat(nonDefaultParams[0].text).isEqualTo("nmin: 1")


        val noParams = driver.findElements(By.className("no-params"))
        assertThat(noParams.count()).isEqualTo(1)
        assertThat(noParams[0].text).isEqualTo("No parameters")

        val noNonDefaultParams = driver.findElements(By.className("no-non-default-params"))
        assertThat(noNonDefaultParams.count()).isEqualTo(1)
        assertThat(noNonDefaultParams[0].text).isEqualTo("No non-default parameters")

        val defaultParamsCount = driver.findElements(By.className("default-params"))
        assertThat(defaultParamsCount.count()).isEqualTo(1)
        defaultParams = driver.findElement(By.className("default-params"))
        collapsedParams = defaultParams.findElement(By.className("collapseSummary"))
        assertThat(collapsedParams.isDisplayed).isFalse()

        showDefault = defaultParams.findElement(By.cssSelector("a"))
        assertThat(showDefault.text).isEqualTo("Show defaults...")
        showDefault.click()

        wait.until(ExpectedConditions.visibilityOf(collapsedParams))
        assertThat(showDefault.text).isEqualTo("Hide defaults...")

        assertThat(defaultParams.findElements(By.className("default-params-collapse"))[0].getAttribute("innerHTML")).isEqualTo("disease: HepB")
        assertThat(defaultParams.findElements(By.className("default-params-collapse"))[1].getAttribute("innerHTML")).isEqualTo("nmin: 0.5")

        showDefault.click()
        wait.until(ExpectedConditions.invisibilityOf(collapsedParams))
        assertThat(showDefault.text).isEqualTo("Show defaults...")
    }

    @Test
    fun `can display workflow summary with depends on and missing dependencies`()
    {
        createWorkflow()
        changeToOtherBranch()
        addReport("use_dependency")
        addReport("use_dependency_2")
        addReport("global")

        val nextButton = driver.findElement(By.id("next-workflow"))
        assertThat(nextButton.isEnabled).isTrue()
        nextButton.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("dependencies")))
        val dependencies = driver.findElements(By.className("dependencies"))
        assertThat(dependencies.count()).isEqualTo(2)
        val reports = driver.findElements(By.className("single-workflow-summary-area"))
        assertThat(reports.count()).isEqualTo(3)
        assertThat(reports[0].findElement(By.cssSelector(".missingDependency span")).text).isEqualTo("Missing dependency")
        assertThat(reports[0].findElement(By.cssSelector(".missingDependency p")).text).isEqualTo("other")
        assertThat(reports[0].findElements(By.cssSelector(".dependsOn")).count()).isEqualTo(0)
        assertThat(reports[1].findElement(By.cssSelector(".dependsOn span")).text).isEqualTo("Depends on")
        assertThat(reports[1].findElement(By.cssSelector(".dependsOn p")).text).isEqualTo("use_dependency")
        assertThat(reports[1].findElements(By.cssSelector(".missingDependency")).count()).isEqualTo(0)
        assertThat(reports[2].findElements(By.cssSelector(".dependsOn")).count()).isEqualTo(0)
        assertThat(reports[2].findElements(By.cssSelector(".missingDependency")).count()).isEqualTo(0)
    }

}
