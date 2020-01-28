package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport

class ReportPagePermissionTests : SeleniumTest()
{
    private val globalTestUser = "Test User"
    private val testListItemUser = "user@example.com"
    private val testRole = "test-role"

    private fun canViewReportReaders(widgetId: String)
    {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#$widgetId li")))
        val listItems = driver.findElements(By.cssSelector("#$widgetId li"))

        assertThat(listItems.count()).isEqualTo(1)
        assertThat(listItems[0].findElement(By.cssSelector("span.display-name")).text)
                .isEqualTo(testListItemUser)
    }


    private fun canViewGlobalReportReaders(widgetId: String)
    {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#$widgetId li")))
        val listItems = driver.findElements(By.cssSelector("#$widgetId li"))

        //Expect to find the global test user as well
        assertThat(listItems.count()).isEqualTo(2)
        assertThat(listItems[0].findElement(By.cssSelector("span.display-name")).text)
                .isEqualTo(globalTestUser)
        assertThat(listItems[1].findElement(By.cssSelector("span.display-name")).text)
                .isEqualTo(testListItemUser)
    }

    private fun canViewReportReaderRoles(widgetId: String)
    {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#$widgetId .role-name")))
        val listItems = driver.findElements(By.cssSelector("#$widgetId .role-name"))

        assertThat(listItems.count()).isEqualTo(1)
        assertThat(listItems[0].text).isEqualTo(testRole)
    }

    private fun canAddUserGroup(widgetId: String, userGroup: String) {

        var listItems = driver.findElements(By.cssSelector("#$widgetId li"))
        assertThat(listItems.count()).isEqualTo(0)

        val addReaderInput = driver.findElement(By.cssSelector("#$widgetId input"))
        addReaderInput.sendKeys(userGroup)
        val addReaderButton = driver.findElement(By.cssSelector("#$widgetId button"))
        addReaderButton.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("li[id='$userGroup']")))

        listItems = driver.findElements(By.cssSelector("#$widgetId > ul > li"))
        assertThat(listItems.count()).isEqualTo(1)

        assertThat(listItems[0].getAttribute("id")).isEqualTo(userGroup)
    }

    private fun canRemoveUserGroup(widgetId: String) {
        //let existing readers load first
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#$widgetId > ul > li")))

        val removeReader = driver.findElement(By.cssSelector("#$widgetId  span.remove"))
        removeReader.click()

        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("#$widgetId  li"), 0))

        val listItems = driver.findElements(By.cssSelector("#$widgetId li"))
        assertThat(listItems.count()).isEqualTo(0)
    }

    @Before
    fun setupPage()
    {
        startApp("auth.provider=montagu")
        insertReport("testreport", "20170103-143015-1234abcd")

        addUserWithPermissions(listOf(
                ReifiedPermission("users.manage", Scope.Global()),
                ReifiedPermission("reports.read", Scope.Global())
        ))

        addUserWithPermissions(listOf(), testListItemUser)
    }

    @Test
    fun `can view scoped report readers`()
    {
        giveUserPermissions(testListItemUser,
                ReifiedPermission("reports.read", Scope.Specific("report", "testreport")))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd")

        canViewReportReaders("scoped-report-readers-list")
    }

    @Test
    fun `can view scoped report reader roles`()
    {
        addUserGroupWithPermissions(testRole,
                listOf(testListItemUser),
                listOf(ReifiedPermission("reports.read", Scope.Specific("report", "testreport"))))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd")

        canViewReportReaderRoles("scoped-roles-list")
    }

    @Test
    fun `can view global report readers`()
    {
        giveUserPermissions(testListItemUser,
                ReifiedPermission("reports.read", Scope.Global()))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd")

        canViewGlobalReportReaders("global-report-readers-list")
    }

    @Test
    fun `can view global report reader roles`()
    {
        addUserGroupWithPermissions(testRole,
                listOf(testListItemUser),
                listOf(ReifiedPermission("reports.read", Scope.Global())))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd")

        canViewReportReaderRoles("report-readers-global-list")
    }

    @Test
    fun `can add report reader`()
    {
        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd")

        canAddUserGroup("scoped-report-readers-list", testListItemUser)
    }

    @Test
    fun `can remove report reader`()
    {
        addUserWithPermissions(listOf(
                ReifiedPermission("reports.read", Scope.Specific("report", "testreport"))
        ), "read.perms@example.com")

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd")

        canRemoveUserGroup("scoped-report-readers-list")
    }

    @Test
    fun `can add report reader role`()
    {
        addUserGroupWithPermissions("new-group", listOf(testListItemUser), listOf())

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd")

        canAddUserGroup("scoped-roles-list", "new-group")
    }

    @Test
    fun `can remove role`()
    {
        addUserGroupWithPermissions(testRole,
                listOf(testListItemUser),
                listOf(ReifiedPermission("reports.read", Scope.Specific("report", "testreport"))))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd")
        canRemoveUserGroup("scoped-roles-list")
    }
}
