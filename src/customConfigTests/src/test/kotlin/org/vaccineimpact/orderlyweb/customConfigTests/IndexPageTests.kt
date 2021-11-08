package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.*
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
            it.dsl.select(ORDERLYWEB_REPORT_VERSION_FULL.REPORT)
                    .from(ORDERLYWEB_REPORT_VERSION_FULL)
                    .where(ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED.eq(true))
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
    fun `can set pinned reports with permission`()
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

    @Test
    fun `cannot set pinned reports without permission`()
    {
        setUpDb()
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(
                ReifiedPermission("reports.read", Scope.Global())))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl)

        val component = driver.findElements(By.cssSelector("#setPinnedReportsVueApp"))
        assertThat(component.count()).isEqualTo(0)
    }

    @Test
    fun `can link to run report page with permission`()
    {
        setUpDb()
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(
                ReifiedPermission("reports.run", Scope.Global())))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl)

        val component = driver.findElements(By.id("run-report"))
        assertThat(component.count()).isEqualTo(1)
        component[0].click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#report")))
    }

    @Test
    fun `can link to run workflow page with permission`()
    {
        setUpDb()
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(
                ReifiedPermission("reports.run", Scope.Global())))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl)

        val component = driver.findElements(By.id("run-workflow"))
        assertThat(component.count()).isEqualTo(1)
        component[0].click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#workflow-container")))
    }

    @Test
    fun `does not link to run workflow page without run permission`()
    {
        setUpDb()
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(
                ReifiedPermission("reports.read", Scope.Global())))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl)

        val component = driver.findElements(By.id("run-workflow"))
        assertThat(component.count()).isEqualTo(0)
    }

    @Test
    fun `can not link to run report page without permission`()
    {
        setUpDb()
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(
                ReifiedPermission("reports.read", Scope.Global())))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl)

        val component = driver.findElements(By.id("run-report"))
        assertThat(component.count()).isEqualTo(0)
    }
}
