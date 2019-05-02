package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.openqa.selenium.By
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

        var toggleButton = driver.findElement(By.cssSelector("[data-toggle=toggle]"))
        assertThat(toggleButton.getAttribute("class").contains("off")).isTrue()

        toggleButton.click()

        Thread.sleep(500)
        toggleButton = driver.findElement(By.cssSelector("[data-toggle=toggle]"))
        assertThat(toggleButton.getAttribute("class").contains("off")).isFalse()
    }

    // TODO replace with unit test
    @Test
    fun `report readers do not see publish switch`()
    {
        startApp("auth.provider=montagu")
        insertReport("testreport", "v1", published = false)

        JooqContext().use {
            insertUserAndGroup(it, "test.user@example.com")
            giveUserGlobalPermission(it, "test.user@example.com", "reports.read")
        }

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/reports/testreport/v1/")

        val publishSwitch = driver.findElements(By.cssSelector("#publish-switch"))
        assertThat(publishSwitch.count()).isEqualTo(0)
    }

}
