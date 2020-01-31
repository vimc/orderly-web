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
        val roleList = driver.findElement(By.id("role-list"))
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

    @Test
    fun `can add role, and edit members`()
    {
        //Add role
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("role-list")))
        val input = driver.findElement(By.cssSelector("#role-list input[placeholder='role name']"))
        input.sendKeys("NewRole")
        val button = driver.findElement(By.id("add-role-btn"))
        button.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#role-list li[id='NewRole']")))

        //Add member
        val roleListItem =  driver.findElement(By.cssSelector("#role-list li[id='NewRole']"))
        roleListItem.findElement(By.cssSelector("span")).click()

        val memberInput = roleListItem.findElement(By.cssSelector("input[placeholder='email']"))
        memberInput.sendKeys("test.user@example.com")
        val addMemberButton = roleListItem.findElement(By.cssSelector("button"))
        addMemberButton.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#role-list li[id='NewRole'] li[id='test.user@example.com']")))

        //Remove member
        val memberListItem = roleListItem.findElement(By.cssSelector("li[id='test.user@example.com'"))
        val removeButton = memberListItem.findElement(By.cssSelector("span.remove"))
        removeButton.click()
        wait.until(ExpectedConditions.not(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("#role-list li[id='NewRole'] li[id='test.user@example.com']")
                )))
    }
}