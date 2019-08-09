package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.REPORT_VERSION
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.test_helpers.insertUserAndGroup
import java.util.regex.Pattern

class ReportPageTests : SeleniumTest()
{
    @Test
    fun `can publish report`()
    {
        startApp("auth.provider=montagu")
        val unpublishedVersion = JooqContext().use {

            it.dsl.select(REPORT_VERSION.ID, REPORT_VERSION.REPORT)
                    .from(REPORT_VERSION)
                    .where(REPORT_VERSION.PUBLISHED.eq(false))
                    .fetchAny()
        }

        val versionId = unpublishedVersion[REPORT_VERSION.ID]
        val reportName = unpublishedVersion[REPORT_VERSION.REPORT]

        JooqContext().use {
            insertUserAndGroup(it, "test.user@example.com")
            giveUserGlobalPermission(it, "test.user@example.com", "reports.read")
            giveUserGlobalPermission(it, "test.user@example.com", "reports.review")
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
    fun `can run report`()
    {
        startApp("auth.provider=montagu")

        val versionRecord = JooqContext().use {

            it.dsl.select(REPORT_VERSION.ID, REPORT_VERSION.REPORT)
                    .from(REPORT_VERSION)
                    .fetchAny()
        }

        val versionId = versionRecord[REPORT_VERSION.ID]
        val reportName = versionRecord[REPORT_VERSION.REPORT]

        JooqContext().use {
            insertUserAndGroup(it, "test.user@example.com")
            giveUserGlobalPermission(it, "test.user@example.com", "reports.read")
            giveUserGlobalPermission(it, "test.user@example.com", "reports.review")
            giveUserGlobalPermission(it, "test.user@example.com", "reports.run")
        }

        loginWithMontagu()
        val versionUrl = RequestHelper.webBaseUrl + "/report/$reportName/$versionId/"
        driver.get(versionUrl)

        val runButton = driver.findElement(By.cssSelector("#run-report button[type=submit]"))
        runButton.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#confirm-run-btn")))
        driver.findElement(By.cssSelector("#confirm-run-btn")).click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#run-report-new-version")))
        wait.until(ExpectedConditions.textMatches(By.cssSelector("#run-report-status"), Pattern.compile(".*success.*")))

        val savedStatusText = driver.findElement(By.cssSelector("#run-report-status")).text
        val savedNewVersionText = driver.findElement(By.cssSelector("#run-report-new-version")).text

        assertThat(savedStatusText).contains("Running status: success")

        //Check state is saved to session - navigate away from page and back again
        driver.get(RequestHelper.webBaseUrl)
        driver.get(versionUrl)

        assertThat(driver.findElement(By.cssSelector("#run-report-status")).text).isEqualTo(savedStatusText)
        assertThat(driver.findElement(By.cssSelector("#run-report-new-version")).text).isEqualTo(savedNewVersionText)
    }

    @Test
    fun `report running status errors are persisted`()
    {
        startApp("auth.provider=montagu\norderly.server=http://nonsense")

        val versionRecord = JooqContext().use {

            it.dsl.select(REPORT_VERSION.ID, REPORT_VERSION.REPORT)
                    .from(REPORT_VERSION)
                    .fetchAny()
        }

        val versionId = versionRecord[REPORT_VERSION.ID]
        val reportName = versionRecord[REPORT_VERSION.REPORT]

        JooqContext().use {
            insertUserAndGroup(it, "test.user@example.com")
            giveUserGlobalPermission(it, "test.user@example.com", "reports.read")
            giveUserGlobalPermission(it, "test.user@example.com", "reports.review")
            giveUserGlobalPermission(it, "test.user@example.com", "reports.run")
        }

        loginWithMontagu()
        val versionUrl = RequestHelper.webBaseUrl + "/report/$reportName/$versionId/"
        driver.get(versionUrl)

        val runButton = driver.findElement(By.cssSelector("#run-report button[type=submit]"))
        runButton.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#confirm-run-btn")))
        driver.findElement(By.cssSelector("#confirm-run-btn")).click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#run-report-status")))

        Thread.sleep(200)

        val savedStatusText = driver.findElement(By.cssSelector("#run-report-status")).text
        val savedNewVersionText = driver.findElements(By.cssSelector("#run-report-new-version"))

        assertThat(savedStatusText).contains("Running status: Error when running report")
        assertThat(savedNewVersionText.count()).isEqualTo(0)

        //Check state is saved to session - navigate away from page and back again
        driver.get(RequestHelper.webBaseUrl)
        driver.get(versionUrl)

        assertThat(driver.findElement(By.cssSelector("#run-report-status")).text).isEqualTo(savedStatusText)
        assertThat(driver.findElements(By.cssSelector("#run-report-new-version")).count()).isEqualTo(0)
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
        confirmTabActive("report", true)
        confirmTabActive("downloads", false)

        //Change to Downloads tab
        val downloadsLink = driver.findElement(By.cssSelector("a[href='#downloads']"))
        downloadsLink.click()
        Thread.sleep(500)
        confirmTabActive("report", false)
        confirmTabActive("downloads", true)
        assertThat(driver.currentUrl).isEqualTo(RequestHelper.webBaseUrl + "/report/testreport/20170103-143015-1234abcd/#downloads")

        //And back to Report
        val reportLink = driver.findElement(By.cssSelector("a[href='#report']"))
        reportLink.click()
        Thread.sleep(500)
        confirmTabActive("report", true)
        confirmTabActive("downloads", false)
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

        confirmTabActive("report", false)
        confirmTabActive("downloads", true)
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
        val tabLink = driver.findElement(By.cssSelector("a[href='#${tabId}']"))
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
