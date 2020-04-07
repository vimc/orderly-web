package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport

class GuestUserTests: SeleniumTest() {

    private fun login()
    {
       wait.until(ExpectedConditions.presenceOfElementLocated(By.id("login_field")))
       loginWithGithub()
    }

    @Before
    fun setUp()
    {
        JooqContext().use {
           it.dsl.update(Tables.ORDERLYWEB_SETTINGS)
                   .set(Tables.ORDERLYWEB_SETTINGS.AUTH_ALLOW_GUEST, true)
                   .execute()
        }
    }

    @Test
    fun `guest user can access homepage`() {
        startApp("auth.provider=github")

        driver.get(RequestHelper.webBaseUrl)

        val header = driver.findElement(By.cssSelector("h1"))
        assertThat(header.text).isEqualTo("Find a report")

        // guest user should not see logout link, but should see login link
        assertThat(driver.findElements(By.className("logout")).count()).isEqualTo(0)
        assertThat(driver.findElements(By.className("login")).count()).isEqualTo(1)
    }

    @Test
    fun `guest user permisisons are updated immediately`() {

        startApp("auth.provider=github")
        driver.get(RequestHelper.webBaseUrl)

        val header = driver.findElement(By.cssSelector("h1"))
        assertThat(header.text).isEqualTo("Find a report")

        // guest user should not see logout link, but should see login link
        assertThat(driver.findElements(By.className("logout")).count()).isEqualTo(0)
        assertThat(driver.findElements(By.className("login")).count()).isEqualTo(1)

        // guest user should not see any reports in report table
        var rows = driver.findElements(By.cssSelector("table.dataTable tbody tr"))
        assertThat(rows.count()).isEqualTo(1) // there's always 1 empty row

        insertReport("testreport", "20170103-143015-1234abcd")
        insertReport("testreport2", "20180103-143015-1234abcd")

        OrderlyAuthorizationRepository().createUserGroup("guest")
        OrderlyAuthorizationRepository()
                .ensureUserGroupHasPermission("guest", ReifiedPermission("reports.read", Scope.Global()))

        driver.get(RequestHelper.webBaseUrl)

        // user should see reports in report table
        rows = driver.findElements(By.cssSelector("table.dataTable tbody tr"))
        assertThat(rows.count()).isEqualTo(2)
    }

    @Test
    fun `guest user can login to escalate privileges`() {

        insertReport("testreport", "20170103-143015-1234abcd")
        insertReport("testreport2", "20180103-143015-1234abcd")
        addUserWithPermissions(listOf(ReifiedPermission("reports.read", Scope.Global())), "notarealemail")

        startApp("auth.provider=github")
        driver.get(RequestHelper.webBaseUrl)

        // guest user should not see any reports in report table
        var rows = driver.findElements(By.cssSelector("table.dataTable tbody tr"))
        assertThat(rows.count()).isEqualTo(1) // there's always 1 empty row
        println("Got expected guest response")

        driver.findElement(By.className("login")).findElement(By.cssSelector("a")).click()
        println("Clicked on  login link")

        // should be redirected to login page with link
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("login-link")))
        driver.findElement(By.className("login-link")).click()
        println("Clicked on  login with github link")

        login()
        println("Logged in")

        // should be returned to the site as logged in user
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("site-title")))
        println("Returned as logged in user")

        // user should see logout link and should no longer see login link
        assertThat(driver.findElements(By.className("login")).count()).isEqualTo(0)
        assertThat(driver.findElements(By.className("logout")).count()).isEqualTo(1)
        assertThat(driver.findElement(By.className("logout")).text).isEqualTo("Logged in as notarealemail | Logout")
        println("Saw expected response for logged in user")

        // user should see reports in report table
        rows = driver.findElements(By.cssSelector("table.dataTable tbody tr"))
        assertThat(rows.count()).isEqualTo(2)
    }

    @Test
    fun `guest user with existing session is redirected to login page next request after allow guest set to false`()
    {
        startApp("auth.provider=github")

        driver.get(RequestHelper.webBaseUrl)

        val header = driver.findElement(By.cssSelector("h1"))
        assertThat(header.text).isEqualTo("Find a report")

        JooqContext().use {
            it.dsl.update(Tables.ORDERLYWEB_SETTINGS)
                    .set(Tables.ORDERLYWEB_SETTINGS.AUTH_ALLOW_GUEST, false)
                    .execute()
        }

        driver.get(RequestHelper.webBaseUrl)
        val button = driver.findElement(By.cssSelector(".login-link"))
        assertThat(button.text).isEqualToIgnoringWhitespace("Log in with GitHub")
    }
}
