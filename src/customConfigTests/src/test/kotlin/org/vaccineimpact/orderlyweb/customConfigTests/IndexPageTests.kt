package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
    fun `can filter datatable by author`()
    {
        setUpDb()
        startApp("auth.provider=montagu")

        addUserWithPermissions(listOf(ReifiedPermission("reports.read", Scope.Global())))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl)

        val allReportRows = driver.findElements(By.cssSelector("table.dataTable tbody tr"))
        assertThat(allReportRows.count()).isGreaterThan(1)

        // Enter known author name
        val input = driver.findElement(By.cssSelector("input#author-filter"))
        input.sendKeys("Dr Very")

        // We expect only one report to have this author name
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("table.dataTable tbody tr"), 1))
        assertThat(driver.findElement(By.cssSelector("tbody tr.has-child td:nth-child(2)")).text).startsWith("Report using a dependency")

        val rowExpander = driver.findElement(By.cssSelector("tbody tr.has-child div.expander"))
        rowExpander.click()

        val authorCells = driver.findElements(By.cssSelector("tbody tr.has-parent td:nth-child(6)"))
        assertThat(authorCells.count()).isGreaterThan(1)
        authorCells.forEach{ assertThat(it.text).isEqualTo("Dr Very Serious") }
    }

    /*@Test
    fun `can filter datatable by parameter`()
    {}*/

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
        component[0].click()
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
        component[0].click()
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
