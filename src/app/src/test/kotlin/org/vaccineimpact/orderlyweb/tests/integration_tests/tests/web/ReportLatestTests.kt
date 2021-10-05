package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import org.vaccineimpact.orderlyweb.test_helpers.insertReportWithPublishedAndUnpublishedVersions
import org.vaccineimpact.orderlyweb.tests.integration_tests.WebPermissionChecker

class ReportLatestTests : IntegrationTest()
{
    @Test
    fun `reviewer can get latest unpublished version`()
    {
        val reportName = "test"
        val publishedVersionId = "20211001-013233-72d597e9"
        val unpublishedVersionId = "20211002-013233-72d597e9"
        insertReportWithPublishedAndUnpublishedVersions(reportName, publishedVersionId, unpublishedVersionId)

        val url = "/report/$reportName/latest"
        val permissions = setOf(
            ReifiedPermission("reports.review", Scope.Global()), 
            ReifiedPermission("reports.read", Scope.Global())
        )
        val checker = WebPermissionChecker(url, permissions)
        val response = checker.requestWithPermissions(permissions)
        Assertions.assertThat(response.statusCode).isEqualTo(200)
        Assertions.assertThat(response.text).contains("$unpublishedVersionId\" selected")
        Assertions.assertThat(response.text).doesNotContain("$publishedVersionId\" selected")
    }

    @Test
    fun `non-reviewer cannot get latest unpublished version, only latest published version`()
    {
        val reportName = "test"
        val publishedVersionId = "20211001-013233-72d597e9"
        val unpublishedVersionId = "20211002-013233-72d597e9"
        insertReportWithPublishedAndUnpublishedVersions(reportName, publishedVersionId, unpublishedVersionId)

        val url = "/report/$reportName/latest"
        val permissions = setOf(
            ReifiedPermission("reports.read", Scope.Global())
        )
        val checker = WebPermissionChecker(url, permissions)
        val response = checker.requestWithPermissions(permissions)
        Assertions.assertThat(response.statusCode).isEqualTo(200)
        Assertions.assertThat(response.text).doesNotContain(unpublishedVersionId)
        Assertions.assertThat(response.text).contains("$publishedVersionId\" selected")
    }
}