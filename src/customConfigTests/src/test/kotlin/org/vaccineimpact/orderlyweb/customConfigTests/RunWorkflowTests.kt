package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.ExpectedConditions.not
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGroupGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertUserAndGroup
import java.time.Duration

class RunWorkflowTests : SeleniumTest()
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
    fun `can create a new workflow and select git branch`()
    {
        createWorkflow()
        val branchSelect = driver.findElement(By.id("git-branch"))
        assertThat(branchSelect.getAttribute("value")).isEqualTo("master")
        val commitSelect = driver.findElement(By.id("git-commit"))
        val commitValue = commitSelect.getAttribute("value")
        assertThat(commitValue).isNotBlank()

        Select(branchSelect).selectByIndex(1)
        assertThat(branchSelect.getAttribute("value")).isEqualTo("other")
        //Default commit value should update when new branch selected
        wait.until(not(ExpectedConditions.attributeToBe(commitSelect, "value", commitValue)))
        assertThat(commitSelect.getAttribute("value")).isNotBlank()
    }

    @Test
    fun `can add and remove reports`()
    {
        createWorkflow()
        val nextButton = driver.findElement(By.id("next-workflow"))
        assertThat(nextButton.isEnabled).isEqualTo(false)
        driver.findElement(By.cssSelector("#workflow-report input")).sendKeys("global")
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#workflow-report a")))
        driver.findElement(By.cssSelector("#workflow-report a")).click()
        val addButton = driver.findElement(By.id("add-report-button"))
        wait.until(ExpectedConditions.elementToBeClickable(addButton))
        addButton.click()

        wait.withTimeout(Duration.ofSeconds(10))
        val wizard = driver.findElement(By.id("workflow-wizard"))
        val stuff = wizard.getAttribute("innerHTML")
        println("stuff: " + stuff)

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".remove-report-button")))
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("workflow-report-0")))
        assertThat(driver.findElement(By.cssSelector("#workflow-report-0 label")).text).isEqualTo("global")
        assertThat(driver.findElement(By.cssSelector("#workflow-report-0 .text-secondary")).text).isEqualTo("No parameters")
        assertThat(nextButton.isEnabled()).isEqualTo(true)

        driver.findElement(By.cssSelector(".remove-report-button")).click()
        wait.until(not(ExpectedConditions.presenceOfElementLocated(By.id("workflow-report-0"))))
        assertThat(nextButton.isEnabled()).isEqualTo(false)
    }

    @Test
    fun `can set parameter value`()
    {
        createWorkflow()
    }

    @Test
    fun `can change branch and see resulting workflow change`()
    {}

    @Test
    fun `can progress to finalise step`()
    {}

    private fun createWorkflow()
    {
        val createButton = driver.findElement(By.id("create-workflow"))
        createButton.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("git-branch")))
    }
}
