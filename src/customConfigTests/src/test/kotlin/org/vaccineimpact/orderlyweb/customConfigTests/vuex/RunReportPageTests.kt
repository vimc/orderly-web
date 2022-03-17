package org.vaccineimpact.orderlyweb.customConfigTests.vuex

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.vaccineimpact.orderlyweb.customConfigTests.RequestHelper
import org.vaccineimpact.orderlyweb.customConfigTests.SeleniumTest
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.test_helpers.giveUserGroupGlobalPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertUserAndGroup

class RunReportPageTests: SeleniumTest()
{
    @Before
    fun setUp()
    {
        JooqContext().use {
            insertUserAndGroup(it, "test.user@example.com")
            giveUserGroupGlobalPermission(it, "test.user@example.com", "reports.run")
        }

        startApp("auth.provider=montagu")

        loginWithMontagu()

        val url = RequestHelper.webBaseUrl + "/vuex-run-report/"
        driver.get(url)
    }

    @Test
    fun `can view vuex run report page`()
    {
        val tag = driver.findElement(By.tagName("h1"))
        assertThat(tag.text).isEqualTo("Vuex Run Report Page")
    }

}
