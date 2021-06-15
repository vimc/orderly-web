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
        Assertions.assertThat(tab.findElement(By.tagName("h2")).text).isEqualTo("Run workflow")
    }

    @Test
    fun `can create a new workflow`()
    {
        val button = driver.findElement(By.id("create-workflow"))
        button.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("git-branch")))
        val branchSelect = driver.findElement(By.id("git-branch"))
        Assertions.assertThat(branchSelect.getAttribute("value")).isEqualTo("master")
        val commitSelect = driver.findElement(By.id("git-commit"))
        val commitValue = commitSelect.getAttribute("value")
        Assertions.assertThat(commitValue).isNotBlank()

        Select(branchSelect).selectByIndex(1)
        Assertions.assertThat(branchSelect.getAttribute("value")).isEqualTo("other")
        //Default commit value should update when new branch selected
        wait.until(not(ExpectedConditions.attributeToBe(commitSelect, "value", commitValue)))
        Assertions.assertThat(commitSelect.getAttribute("value")).isNotBlank()
    }

}
