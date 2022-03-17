package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.openqa.selenium.By
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertDocument

class DocumentPageTests : SeleniumTest()
{
    // this is just a basic test that the docs are making it onto the page and into the vue component
    // all other functionality is under test in the front-end unit tests
    @Test
    fun `documents are rendered`()
    {
        startApp("auth.provider=montagu")

        JooqContext().use {
            insertDocument(it, "/name", 0)
            insertDocument(it, "child", 1, "/name")
        }

        addUserWithPermissions(listOf(ReifiedPermission("documents.read", Scope.Global())))

        loginWithMontagu()
        driver.get(RequestHelper.webBaseUrl + "/project-docs/")

        Assertions.assertThat(driver.findElement(By.cssSelector(".folder-name")).text).isEqualTo("/name")
        Assertions.assertThat(driver.findElements(By.cssSelector(".octicon-file")).count()).isEqualTo(1)
    }
}
