package org.vaccineimpact.orderlyweb.tests.selenium

import org.assertj.core.api.Assertions
import org.junit.Test

class ReportPageTests: SeleniumTest<ReportPage>(ReportPage::class) {

    @Test
    fun canPublish()
    {
        startingPage.togglePublish()
        Assertions.assertThat(startingPage.publishButton.getAttribute("class"))
                .doesNotContain("off")

        startingPage.togglePublish()
        Assertions.assertThat(startingPage.publishButton.getAttribute("class"))
                .contains("off")
    }
}