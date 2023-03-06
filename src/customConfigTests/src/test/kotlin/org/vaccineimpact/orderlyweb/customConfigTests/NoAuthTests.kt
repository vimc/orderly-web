package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.http.HttpClient
import org.vaccineimpact.orderlyweb.test_helpers.insertReport

class NoAuthTests : SeleniumTest()
{
    @Test
    fun `no user needed to get included routes`()
    {
        startApp("auth=false")
        val response = HttpClient.get("/reports/minimal", mapOf())
        assertSuccessful(response)
    }

    @Test
    fun `excluded routes return 404`()
    {
        startApp("auth=false")
        val response = HttpClient.get("/reports/runnable", mapOf())
        assertThat(response.statusCode).isEqualTo(404)
    }

    @Test
    fun `does not link to run workflow page without auth`()
    {
        startApp("auth=false")

        driver.get(RequestHelper.webBaseUrl)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("site-title")))

        val component = driver.findElements(By.id("run-workflow"))
        assertThat(component.count()).isEqualTo(0)
    }

    @Test
    fun `does not link to run report page if no auth`()
    {
        startApp("auth=false")

        driver.get(RequestHelper.webBaseUrl)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("site-title")))

        val component = driver.findElements(By.id("run-report"))
        assertThat(component.count()).isEqualTo(0)
    }
}
