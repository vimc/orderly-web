package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport

class AnonUserTests: SeleniumTest() {

    @Test
    fun `anon user can access homepage`() {

        startApp("auth.allow_anon=true")
        driver.get(RequestHelper.webBaseUrl)

        val header = driver.findElement(By.cssSelector("h1"))
        assertThat(header.text).isEqualTo("Find a report")

        // anon user should not see logout link, but should see login link
        assertThat(driver.findElements(By.className("logout")).count()).isEqualTo(0)
        assertThat(driver.findElements(By.className("login")).count()).isEqualTo(1)
    }

    @Test
    fun `anon user permisisons are updated immediately`() {

        startApp("auth.allow_anon=true")
        driver.get(RequestHelper.webBaseUrl)

        val header = driver.findElement(By.cssSelector("h1"))
        assertThat(header.text).isEqualTo("Find a report")

        // anon user should not see logout link, but should see login link
        assertThat(driver.findElements(By.className("logout")).count()).isEqualTo(0)
        assertThat(driver.findElements(By.className("login")).count()).isEqualTo(1)

        // anon user should not see any reports in report table
        var rows = driver.findElements(By.cssSelector("table.dataTable tbody tr"))
        assertThat(rows.count()).isEqualTo(1) // there's always 1 empty row

        insertReport("testreport", "20170103-143015-1234abcd")
        insertReport("testreport2", "20180103-143015-1234abcd")

        OrderlyAuthorizationRepository().createUserGroup("anon")
        OrderlyAuthorizationRepository()
                .ensureUserGroupHasPermission("anon", ReifiedPermission("reports.read", Scope.Global()))

        driver.get(RequestHelper.webBaseUrl)

        // user should see reports in report table
        rows = driver.findElements(By.cssSelector("table.dataTable tbody tr"))
        assertThat(rows.count()).isEqualTo(2)
    }

    @Test
    fun `anon user can login to escalate privileges`() {

        insertReport("testreport", "20170103-143015-1234abcd")
        insertReport("testreport2", "20180103-143015-1234abcd")
        addUserWithPermissions(listOf(ReifiedPermission("reports.read", Scope.Global())))

        startApp("auth.allow_anon=true")
        driver.get(RequestHelper.webBaseUrl)

        // anon user should not see any reports in report table
        var rows = driver.findElements(By.cssSelector("table.dataTable tbody tr"))
        assertThat(rows.count()).isEqualTo(1) // there's always 1 empty row

        driver.findElement(By.className("login")).findElement(By.cssSelector("a")).click()

        // should be redirected to login page with link
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("login-link")))
        driver.findElement(By.className("login-link")).click()

        // should be directed to login provider, i.e. Montagu in this case
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")))

        driver.findElement(By.name("email")).sendKeys("test.user@example.com")
        driver.findElement(By.name("password")).sendKeys("password")
        driver.findElement(By.id("login-button")).click()

        // should be returned to the site as logged in user
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("site-title")))

        // user should see logout link and should no longer see login link
        assertThat(driver.findElements(By.className("login")).count()).isEqualTo(0)
        assertThat(driver.findElements(By.className("logout")).count()).isEqualTo(1)

        // user should see reports in report table
        rows = driver.findElements(By.cssSelector("table.dataTable tbody tr"))
        assertThat(rows.count()).isEqualTo(2)
    }
}
