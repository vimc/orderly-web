package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
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
        insertReport("testreport", "20170103-143015-1234abcd")
        loginWithMontagu()

        driver.get(RequestHelper.webBaseUrl + "/reports/testreport/20170103-143015-1234abcd/")
        assertThat(driver.findElement(By.cssSelector("h1")).text).isEqualTo("Page not found")

        logout()
        OrderlyAuthorizationRepository()
                .ensureUserGroupHasPermission("test.user@example.com",
                        ReifiedPermission("reports.read", Scope.Global()))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/reports/testreport/20170103-143015-1234abcd/")

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
    fun `can change tabs`()
    {
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(ReifiedPermission("reports.read", Scope.Global())))

        insertReport("testreport", "20170103-143015-1234abcd")

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/reports/testreport/20170103-143015-1234abcd/")

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

    @Test
    fun `can switch version`()
    {
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(ReifiedPermission("reports.read", Scope.Global())))

        insertReport("testreport", "20170103-143015-1234abcd")
        insertReport("testreport", "20170104-091500-1234dcba")

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/reports/testreport/20170104-091500-1234dcba")

        val versionSwitcher = Select(driver.findElement(By.cssSelector("#report-version-switcher")))
        versionSwitcher.selectByVisibleText("Tue Jan 03 2017, 14:30")
        wait.until(ExpectedConditions.urlMatches(RequestHelper.webBaseUrl + "/reports/testreport/20170103-143015-1234abcd"))
        assertThat(driver.findElement(By.cssSelector("p.small.text-muted")).text).isEqualTo("20170103-143015-1234abcd")
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
