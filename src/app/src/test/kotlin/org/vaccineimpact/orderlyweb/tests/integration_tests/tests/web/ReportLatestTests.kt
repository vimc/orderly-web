package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.jsoup.Jsoup
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.integration_tests.WebPermissionChecker
import java.text.SimpleDateFormat
import java.sql.Timestamp

class ReportLatestTests : IntegrationTest()
{
    @Test
    fun `reviewer can get latest unpublished version`()
    {
        val reportName = "test"
        val publishedVersionId = "20211001-013233-72d597e9"
        val unpublishedVersionId = "20211002-013233-72d597e9"
        insertReport(reportName, publishedVersionId, true, Timestamp(SimpleDateFormat("dd-MM-yyyy").parse("14-02-2018").time))
        insertReport(reportName, unpublishedVersionId, false, Timestamp(SimpleDateFormat("dd-MM-yyyy").parse("15-02-2018").time))

        val url = "/report/$reportName/latest"
        val permissions = setOf(
            ReifiedPermission("reports.review", Scope.Global()), 
            ReifiedPermission("reports.read", Scope.Global())
        )
        val checker = WebPermissionChecker(url, permissions)
        val response = checker.requestWithPermissions(permissions)
        Assertions.assertThat(response.statusCode).isEqualTo(200)
        val page = Jsoup.parse(response.text)
        val selectedOptionURL = page.selectFirst("option[selected]").attr("value")
        Assertions.assertThat(selectedOptionURL).contains(unpublishedVersionId)
    }

    @Test
    fun `non-reviewer cannot get latest unpublished version, only latest published version`()
    {
        val reportName = "test"
        val publishedVersionId = "20211001-013233-72d597e9"
        val unpublishedVersionId = "20211002-013233-72d597e9"
        insertReport(reportName, publishedVersionId, true, Timestamp(SimpleDateFormat("dd-MM-yyyy").parse("14-02-2018").time))
        insertReport(reportName, unpublishedVersionId, false, Timestamp(SimpleDateFormat("dd-MM-yyyy").parse("15-02-2018").time))

        val url = "/report/$reportName/latest"
        val permissions = setOf(
            ReifiedPermission("reports.read", Scope.Global())
        )
        val checker = WebPermissionChecker(url, permissions)
        val response = checker.requestWithPermissions(permissions)
        Assertions.assertThat(response.statusCode).isEqualTo(200)
        val page = Jsoup.parse(response.text)
        val selectedOptionURL = page.selectFirst("option[selected]").attr("value")
        Assertions.assertThat(selectedOptionURL).contains(publishedVersionId)
    }
}