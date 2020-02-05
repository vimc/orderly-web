package org.vaccineimpact.orderlyweb.customConfigTests

import org.junit.Test
import org.junit.Before
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.assertj.core.api.Assertions.assertThat
import org.openqa.selenium.remote.RemoteWebElement
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGroupGlobalPermission
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
            insertUserAndGroup(it, "another.user@example.com")
            giveUserGroupGlobalPermission(it, "test.user@example.com", "users.manage")
            giveUserGroupGlobalPermission(it, "test.user@example.com", "reports.review")

            insertRole(it, "Funders", "test.user@example.com")
            giveUserGroupGlobalPermission(it, "Funders", "reports.read")
        }

        startApp("auth.provider=montagu")

        loginWithMontagu()

        val adminUrl = RequestHelper.webBaseUrl + "/manage-access/"
        driver.get(adminUrl)
    }

    @Test
    fun `can view roles`()
    {
        val roleList = driver.findElement(By.cssSelector("#manage-roles"))
        val listItems = roleList.findElements(By.cssSelector("li.role"))
        assertThat(listItems.size).isEqualTo(2)
        assertThat(listItems[1].getAttribute("id")).isEqualTo("Funders")

        //expand the role
        listItems[1].findElement(By.tagName("span")).click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#manage-roles li ul li")))
        val memberItems = listItems[1].findElements(By.cssSelector("li"))
        assertThat(memberItems.size).isEqualTo(1)
        assertThat(memberItems[0].getAttribute("id")).isEqualTo("test.user@example.com")
    }

    @Test
    fun `can add role, and edit members`()
    {
        //Add role
        val input = driver.findElement(By.cssSelector("#manage-roles input[placeholder='role name']"))
        input.sendKeys("NewRole")
        val button = driver.findElement(By.id("add-role-btn"))
        button.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#manage-roles li[id='NewRole']")))

        //Add member
        val roleListItem =  driver.findElement(By.cssSelector("#manage-roles li[id='NewRole']"))
        roleListItem.findElement(By.className("role-name")).click()

        val memberInput = roleListItem.findElement(By.cssSelector("input[placeholder='email']"))
        memberInput.sendKeys("test.user@example.com")
        val addMemberButton = roleListItem.findElement(By.tagName("button"))
        addMemberButton.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#manage-roles li[id='NewRole'] li[id='test.user@example.com']")))

        //Remove member
        val memberListItem = roleListItem.findElement(By.cssSelector("li[id='test.user@example.com'"))
        val removeButton = memberListItem.findElement(By.cssSelector("span.remove"))
        removeButton.click()
        wait.until(ExpectedConditions.not(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("#manage-roles li[id='NewRole'] li[id='test.user@example.com']")
                )))
    }

    @Test
    fun `can see search results when adding user to role`()
    {
        val roleListItem =  driver.findElement(By.cssSelector("#manage-roles li[id='Funders']"))

        roleListItem.findElement(By.className("role-name")).click()

        val memberInput = roleListItem.findElement(By.cssSelector("input[placeholder='email']"))
        memberInput.sendKeys("ano")

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#manage-roles li[id='Funders'] .vbt-autcomplete-list")))
        val autoList = roleListItem.findElement(By.className("vbt-autcomplete-list"))
        val links = autoList.findElements(By.tagName("a"))
        assertThat(links.size).isEqualTo(1)
        assertThat(links[0].text).isEqualTo("another.user@example.com")
    }

    @Test
    fun `can view and edit role permissions`()
    {
        val roleListItem =  driver.findElement(By.cssSelector("#manage-role-permissions li[id='Funders']"))
        roleListItem.findElement(By.tagName("span")).click()

        val permissionItem = roleListItem.findElement(By.tagName("li"))
        assertThat(permissionItem.findElement(By.className("name")).text).isEqualToIgnoringWhitespace("reports.read")

        //remove permission
        permissionItem.findElement(By.className("remove")).click()
        wait.until(ExpectedConditions.not(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("#manage-role-permissions li[id='Funders'] li")
                )))

        //add permission
        val addPermission = roleListItem.findElement(By.className("add-permission"))
        addPermission.findElement(By.tagName("input")).sendKeys("reports.review")
        addPermission.findElement(By.tagName("button")).click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#manage-role-permissions li[id='Funders'] li span[name='reports.review']")))
    }

    @Test
    fun `can see search results when adding permission to role`()
    {
        val roleListItem =  driver.findElement(By.cssSelector("#manage-role-permissions li[id='Funders']"))
        roleListItem.findElement(By.tagName("span")).click()

        val addPermission = roleListItem.findElement(By.className("add-permission"))
        addPermission.findElement(By.tagName("input")).sendKeys("rep")

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#manage-role-permissions li[id='Funders'] .vbt-autcomplete-list")))
        val autoList = roleListItem.findElement(By.className("vbt-autcomplete-list"))
        val links = autoList.findElements(By.tagName("a"))
        assertThat(links.size).isEqualTo(2)
        assertThat(links[0].text).isEqualTo("reports.review")
        assertThat(links[1].text).isEqualTo("reports.run")
    }

    @Test
    fun `can view and edit user permissions`()
    {
        val manageUsers = driver.findElement(By.id("manage-users"))
        val findUsersInput = manageUsers.findElement(By.cssSelector("input"))
        findUsersInput.sendKeys("test")

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#manage-users div.email")))
        val userListItem = manageUsers.findElement(By.className("role"));
        assertThat(userListItem.findElement(By.cssSelector("div.email")).text).isEqualToIgnoringWhitespace("test.user@example.com")

        userListItem.findElement(By.className("role-name")).click()
        val permissionsListItems = userListItem.findElements(By.tagName("li"))
        assertThat(permissionsListItems.size).isEqualTo(3)
        assertThat(permissionsListItems[0].findElement(By.className("name")).text).isEqualTo("reports.read")
        assertThat(permissionsListItems[0].findElement(By.className("source")).text).isEqualTo("(Funders)")
        assertThat(permissionsListItems[1].findElement(By.className("name")).text).isEqualTo("reports.review")
        assertThat(permissionsListItems[2].findElement(By.className("name")).text).isEqualTo("users.manage")

        //remove permission
        permissionsListItems[1].findElement(By.className("remove")).click()
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("#manage-users li li"), 2))

        //add permission
        val addPermission = userListItem.findElement(By.className("add-permission"))
        addPermission.findElement(By.tagName("input")).sendKeys("reports.run")
        addPermission.findElement(By.tagName("button")).click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#manage-users li li span[name='reports.run']")))
    }

    @Test
    fun `can see search results when adding permission to users`()
    {
        val manageUsers = driver.findElement(By.id("manage-users"))
        val findUsersInput = manageUsers.findElement(By.cssSelector("input"))
        findUsersInput.sendKeys("test")

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#manage-users div.email")))
        val userListItem = manageUsers.findElement(By.tagName("li"));
        userListItem.findElement(By.className("role-name")).click()

        val addPermission = userListItem.findElement(By.className("add-permission"))
        addPermission.findElement(By.tagName("input")).sendKeys("rep")

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#manage-users li .vbt-autcomplete-list")))
        val autoList = userListItem.findElement(By.className("vbt-autcomplete-list"))
        val links = autoList.findElements(By.tagName("a"))
        assertThat(links.size).isEqualTo(2)
        assertThat(links[0].text).isEqualTo("reports.read")
        assertThat(links[1].text).isEqualTo("reports.run")
    }
}