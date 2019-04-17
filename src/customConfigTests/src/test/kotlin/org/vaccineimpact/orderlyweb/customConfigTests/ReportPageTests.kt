package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.openqa.selenium.By
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport

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
        insertReport("testreport", "v1", published = false)

        loginWithMontagu()
        logout()

        OrderlyAuthorizationRepository()
                .ensureUserGroupHasPermission("test.user@example.com",
                        ReifiedPermission("reports.read", Scope.Global()))

        OrderlyAuthorizationRepository()
                .ensureUserGroupHasPermission("test.user@example.com",
                        ReifiedPermission("reports.review", Scope.Global()))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/reports/testreport/v1/")

        val toggleButton = driver.findElement(By.cssSelector("[data-toggle=toggle]"))
        assertThat(toggleButton.getAttribute("class").contains("off")).isTrue()

        toggleButton.click()

        assertThat(toggleButton.getAttribute("class").contains("on")).isTrue()
    }

}
