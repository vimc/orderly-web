package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGroupGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertUserAndGroup

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
        Assertions.assertThat(tab.findElement(By.tagName("h2")).text).isEqualTo("Create workflow")
        Assertions.assertThat(tab.findElement(By.tagName("p")).text).isEqualTo("Run workflow is coming soon")
    }

    @Test
    fun `can view run workflow progress tab`()
    {
        driver.findElement(By.id("workflow-progress-link")).click()
        val tab = driver.findElement(By.id("workflow-progress-tab"))
        wait.until(ExpectedConditions.attributeToBe(tab,"display", "block"))
        Assertions.assertThat(tab.findElement(By.tagName("h2")).text).isEqualTo("Workflow progress")
        Assertions.assertThat(tab.findElement(By.tagName("p")).text).isEqualTo("Run workflow progress is coming soon")
    }
}