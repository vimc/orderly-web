package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.jooq.impl.DSL.max
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

class IndexPageTests : SeleniumTest()
{
    @Test
    fun `report reviewers can see reports whose versions are all unpublished`()
    {
        startApp("auth.provider=montagu")

        JooqContext().use {
            insertUserAndGroup(it, "test.user@example.com")
            giveUserGlobalPermission(it, "test.user@example.com", "reports.read")
            giveUserGlobalPermission(it, "test.user@example.com", "reports.review")
        }

        val reportName = getReportWithOnlyUnpublishedVersions()

        loginWithMontagu()

        assertThat(driver.findElements(By.linkText(reportName)).count()).isEqualTo(1)
    }

    @Test
    fun `non report reviewers cannot see reports whose versions are all unpublished`()
    {
        startApp("auth.provider=montagu")

        JooqContext().use {
            insertUserAndGroup(it, "test.user@example.com")
            giveUserGlobalPermission(it, "test.user@example.com", "reports.read")
        }

        val reportName = getReportWithOnlyUnpublishedVersions()

        loginWithMontagu()

        assertThat(driver.findElements(By.linkText(reportName)).count()).isEqualTo(0)
    }

    private fun getReportWithOnlyUnpublishedVersions(): String
    {
        val record = JooqContext().use {

            it.dsl.select(REPORT_VERSION.REPORT)
                    .from(REPORT_VERSION)
                    .groupBy(REPORT_VERSION.REPORT)
                    .having(max(REPORT_VERSION.PUBLISHED).eq(false))
                    .fetchAny()
        }

        return record[REPORT_VERSION.REPORT]
    }

}
