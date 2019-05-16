package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.db.Tables.REPORT_VERSION
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.test_helpers.insertUserAndGroup

class ReportPageTests : SeleniumTest()
{
    @Test
    fun `only report readers can see report page`()
    {
        startApp("auth.provider=montagu")
        insertReport("testreport", "v1")
        loginWithMontagu()

        driver.get(RequestHelper.webBaseUrl + "/reports/testreport/v1/")
        assertThat(driver.findElement(By.cssSelector("h1")).text).isEqualTo("Page not found")

        logout()
        OrderlyAuthorizationRepository()
                .ensureUserGroupHasPermission("test.user@example.com",
                        ReifiedPermission("reports.read", Scope.Global()))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/reports/testreport/v1/")

        assertThat(driver.findElement(By.cssSelector("h1")).text).isEqualTo("display name testreport")
    }

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
        driver.get(RequestHelper.webBaseUrl + "/reports/$reportName/$versionId/")

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
        val versionUrl = RequestHelper.webBaseUrl + "/reports/$reportName/$versionId/"
        driver.get(versionUrl)

        val runButton = driver.findElement(By.cssSelector("#run-report button[type=submit]"))
        runButton.click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#confirm-run-btn")))
        driver.findElement(By.cssSelector("#confirm-run-btn")).click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#run-report-status")))

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#run-report-new-version")))

        val savedStatusText = driver.findElement(By.cssSelector("#run-report-status")).text
        val savedNewVersionText = driver.findElement(By.cssSelector("#run-report-new-version")).text

        assertThat(savedStatusText).isEqualTo("Running status: success")

        //Check state is saved to session - navigate away from page and back again
        driver.get(RequestHelper.webBaseUrl)
        driver.get(versionUrl)

        assertThat(driver.findElement(By.cssSelector("#run-report-status")).text).isEqualTo(savedStatusText)
        assertThat(driver.findElement(By.cssSelector("#run-report-new-version")).text).isEqualTo(savedNewVersionText)
    }

    @Test
    fun `can change tabs`()
    {
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(ReifiedPermission("reports.read", Scope.Global())))

        insertReport("testreport", "v1")

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/reports/testreport/v1/")

        //Confirm that we've started on the Report tab
        confirmTabActive("report-tab", true)
        confirmTabActive("downloads-tab", false)

        //Change to Downloads tab
        val downloadsLink = driver.findElement(By.cssSelector("a[href='#downloads-tab']"))
        downloadsLink.click()
        Thread.sleep(500)
        confirmTabActive("report-tab", false)
        confirmTabActive("downloads-tab", true)

        //And back to Report
        val reportLink = driver.findElement(By.cssSelector("a[href='#report-tab']"))
        reportLink.click()
        Thread.sleep(500)
        confirmTabActive("report-tab", true)
        confirmTabActive("downloads-tab", false)

    }

    private fun confirmTabActive(tabId: String, active: Boolean)
    {
        val tabLink = driver.findElement(By.cssSelector("a[href='#${tabId}']"))
        var expectedLinkClass = "nav-link"
        if (active)
        {
            expectedLinkClass += " active"
        }
        assertThat(tabLink.getAttribute("class")).isEqualTo(expectedLinkClass)

        val tabPane = driver.findElement(By.id(tabId))
        var expectedPaneClass = "tab-pane"
        if (active)
        {
            expectedPaneClass += " active"
        }
        assertThat(tabPane.getAttribute("class")).isEqualTo(expectedPaneClass)
    }

}
