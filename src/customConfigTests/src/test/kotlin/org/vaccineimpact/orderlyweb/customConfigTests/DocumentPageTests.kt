package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.openqa.selenium.By
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.test_helpers.insertDocument

class DocumentPageTests : SeleniumTest()
{
    @Test
    fun `can publish report`()
    {
        startApp("auth.provider=montagu")

        JooqContext().use {
            insertDocument(it, "/name", 0)
            insertDocument(it, "child", 1, "/name")
        }

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/project-docs/")

        val docsList = driver.findElement(By.cssSelector("ul"))
        Assertions.assertThat(docsList.findElement(By.cssSelector("folder-name")).text).isEqualTo("/name")
        Assertions.assertThat(docsList.findElements(By.cssSelector("octicon-file")).count()).isEqualTo(1)
    }

}