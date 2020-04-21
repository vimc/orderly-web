package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.REPORT_VERSION
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport

class IndexPageTests : SeleniumTest()
{
    private fun setUpDb()
    {
        insertReport("testreport", "20170103-143015-1234abcd")
        insertReport("testreport", "20170103-153015-1234abcd")

        insertReport("testreport2", "20180103-143015-1234abcd")
        insertReport("testreport2", "20180204-143015-1234abcd")
    }

    @Test
    fun `datatable is rendered`()
    {
        setUpDb()
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(ReifiedPermission("reports.read", Scope.Global())))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl)

        val reportNames = JooqContext().use {
            it.dsl.select(REPORT_VERSION.REPORT)
                    .from(REPORT_VERSION)
                    .where(REPORT_VERSION.PUBLISHED.eq(true))
                    .fetch().distinct()
        }

        val rows = driver.findElements(By.cssSelector("table.dataTable tbody tr"))
        assertThat(rows.count()).isEqualTo(reportNames.count())
    }

    @Test
    fun `can expand and collapse all rows`()
    {
        setUpDb()
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(ReifiedPermission("reports.read", Scope.Global())))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl)

        val childRowSelector = By.cssSelector("table.dataTable tr.level-1")
        var childRows = driver.findElements(childRowSelector)
        assertThat(childRows.count()).isEqualTo(0)

        driver.findElement(By.cssSelector("#expand")).click()

        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(childRowSelector, 0))

        driver.findElement(By.cssSelector("#collapse")).click()

        wait.until(ExpectedConditions.numberOfElementsToBe(childRowSelector, 0))
        childRows = driver.findElements(childRowSelector)
        assertThat(childRows.count()).isEqualTo(0)
    }

    @Test
    fun `can set pinned reports`()
    {
        setUpDb()
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(
                ReifiedPermission("reports.read", Scope.Global()),
                ReifiedPermission("pinned-reports.manage", Scope.Global())))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl)

        driver.findElement(By.cssSelector("#setPinnedReportsVueApp a")).click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-pinned-report")))

        val addReportField = driver.findElement(By.cssSelector("#setPinnedReportsVueApp input"))
        addReportField.sendKeys("testreport2")
        driver.findElement(By.id("add-pinned-report")).click()

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#setPinnedReportsVueApp #testreport2")))

        driver.findElement(By.cssSelector("#pinned-report-buttons button[type='submit']")).click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#pinned-reports .card")))
        assertThat(driver.findElement(By.cssSelector("#pinned-reports .card a")).text).isEqualTo("testreport2")
    }

}
