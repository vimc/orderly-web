package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGroupGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.test_helpers.insertUserAndGroup

class ReportPageTests : SeleniumTest()
{
    @Test
    fun `can publish report`()
    {
        startApp("auth.provider=montagu")
        val unpublishedVersion = JooqContext().use {

            it.dsl.select(ORDERLYWEB_REPORT_VERSION_FULL.ID, ORDERLYWEB_REPORT_VERSION_FULL.REPORT)
                    .from(ORDERLYWEB_REPORT_VERSION_FULL)
                    .where(ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED.eq(false))
                    .fetchAny()
        }

        val versionId = unpublishedVersion[ORDERLYWEB_REPORT_VERSION_FULL.ID]
        val reportName = unpublishedVersion[ORDERLYWEB_REPORT_VERSION_FULL.REPORT]

        JooqContext().use {
            insertUserAndGroup(it, "test.user@example.com")
            giveUserGroupGlobalPermission(it, "test.user@example.com", "reports.read")
            giveUserGroupGlobalPermission(it, "test.user@example.com", "reports.review")
        }

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/report/$reportName/$versionId/")

        val toggleButton = driver.findElement(By.cssSelector("[data-toggle=toggle]"))
        assertThat(toggleButton.getAttribute("class").contains("off")).isTrue()

        toggleButton.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-toggle='toggle'][class='toggle btn btn-published']")))
    }

    @Test
    fun `can change tabs`()
    {
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(ReifiedPermission("reports.read"
                , Scope.Global())))

        insertReport("testreport", "20170103-143015-1234abcd")

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd/")

        //Confirm that we've started on the Report tab
        confirmTabActive("report-tab", true)
        confirmTabActive("downloads-tab", false)

        //Change to Downloads tab
        val downloadsLink = driver.findElement(By.cssSelector("a[href='#downloads-tab']"))
        downloadsLink.click()
        Thread.sleep(500)
        confirmTabActive("report-tab", false)
        confirmTabActive("downloads-tab", true)
        assertThat(driver.currentUrl).isEqualTo(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd/#downloads")

        //Change to Metadata tab
        val metadataLink = driver.findElement(By.cssSelector("a[href='#metadata-tab']"))
        metadataLink.click()
        Thread.sleep(500)
        confirmTabActive("downloads-tab", false)
        confirmTabActive("metadata-tab", true)
        assertThat(driver.currentUrl).isEqualTo(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd/#metadata")


        //And back to Report
        val reportLink = driver.findElement(By.cssSelector("a[href='#report-tab']"))
        reportLink.click()
        Thread.sleep(500)
        confirmTabActive("report-tab", true)
        confirmTabActive("downloads-tab", false)
        assertThat(driver.currentUrl).isEqualTo(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd/#report")

    }

    @Test
    fun `can deep link to tabs`()
    {
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(ReifiedPermission("reports.read"
                , Scope.Global())))

        insertReport("testreport", "20170103-143015-1234abcd")

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd/#downloads")

        confirmTabActive("report-tab", false)
        confirmTabActive("downloads-tab", true)
    }

    @Test
    fun `can toggle side nav in mobile view`()
    {
        startApp("auth.provider=montagu")

        driver.manage().window().size = Dimension(300, 500)

        addUserWithPermissions(listOf(ReifiedPermission("reports.read", Scope.Global())))

        insertReport("testreport", "20170103-143015-1234abcd")

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd/")

        val sidebar = driver.findElement(By.id("sidebar"))
        assertThat(sidebar.isDisplayed).isFalse()

        driver.findElement(By.cssSelector("[data-toggle=collapse]")).click()
        wait.until(ExpectedConditions.visibilityOf(sidebar))
        assertThat(sidebar.isDisplayed).isTrue()
    }

    @Test
    fun `can navigate to run report page with querystring`()
    {
        JooqContext().use {
            insertUserAndGroup(it, "test.user@example.com")
            giveUserGroupGlobalPermission(it, "test.user@example.com", "reports.read")
            giveUserGroupGlobalPermission(it, "test.user@example.com", "reports.review")
            giveUserGroupGlobalPermission(it, "test.user@example.com", "reports.run")
        }

        startApp("auth.provider=montagu")

        insertReport("testreport", "20170104-091500-1234dcba")
        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/report/testreport/20170104-091500-1234dcba")

        val runReportLink = driver.findElement(By.cssSelector("#run-report-link"))
        runReportLink.click()
        wait.until(ExpectedConditions.urlToBe(RequestHelper.webBaseUrl + "/run-report?report-name=testreport"))
    }

    @Test
    fun `can switch version`()
    {
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(ReifiedPermission("reports.read", Scope.Global())))

        insertReport("testreport", "20170103-143015-1234abcd")
        insertReport("testreport", "20170104-091500-1234dcba")

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/report/testreport/20170104-091500-1234dcba")

        val versionSwitcher = Select(driver.findElement(By.cssSelector("#report-version-switcher")))
        versionSwitcher.selectByVisibleText("Tue Jan 03 2017, 14:30")
        wait.until(ExpectedConditions.urlMatches(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd"))
        assertThat(driver.findElement(By.cssSelector("p.small.text-muted")).text).isEqualTo("20170103-143015-1234abcd")
    }

    private fun confirmTabActive(tabId: String, active: Boolean)
    {
        val tabLink = driver.findElement(By.cssSelector("a[href='#$tabId']"))
        var expectedLinkClasses = arrayOf("nav-link")
        if (active)
        {
            expectedLinkClasses += "active"
        }
        val actualLinkClasses = tabLink.getAttribute("class").split(" ")
        expectedLinkClasses.map {
            assertThat(actualLinkClasses).contains(it)
        }

        val tabPane = driver.findElement(By.id(tabId))
        var expectedPaneClasses = arrayOf("tab-pane")
        if (active)
        {
            expectedPaneClasses += "active"
        }
        val actualPaneClasses = tabPane.getAttribute("class").split(" ")
        expectedPaneClasses.map {
            assertThat(actualPaneClasses).contains(it)
        }
    }

}
