package org.vaccineimpact.orderlyweb.customConfigTests

import org.junit.Test
import org.junit.Before
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.assertj.core.api.Assertions.assertThat
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertUserAndGroup
import org.vaccineimpact.orderlyweb.test_helpers.insertRole
import java.time.Duration

class AdminPageTests : SeleniumTest()
{
    @Before
    fun setUp()
    {
        JooqContext().use {
            insertUserAndGroup(it, "test.user@example.com")
            giveUserGlobalPermission(it, "test.user@example.com", "users.manage")
            insertRole(it, "Funders", "test.user@example.com")
        }

        startApp("auth.provider=montagu")

        loginWithMontagu()

        val adminUrl = RequestHelper.webBaseUrl + "/admin/"
        driver.get(adminUrl)
    }

    @Test
    fun `can view roles`()
    {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#role-list li")))
        val roleList = driver.findElement(By.cssSelector("#role-list"))
        val listItems = roleList.findElements(By.cssSelector("li.role"))
        assertThat(listItems.size).isEqualTo(1)
        assertThat(listItems[0].getAttribute("id")).isEqualTo("Funders")

        //expand the role
        listItems[0].findElement(By.cssSelector("span")).click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#role-list li ul li")))
        val memberItems = listItems[0].findElements(By.cssSelector("li"))
        assertThat(memberItems.size).isEqualTo(1)
        assertThat(memberItems[0].getAttribute("id")).isEqualTo("test.user@example.com")

       // val bodyHtml = driver.findElement(By.cssSelector("body")).getAttribute("innerHTML")
    }
}