package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
// import org.jsoup.Jsoup
import org.junit.Test
// import org.vaccineimpact.orderlyweb.ContentTypes
// import org.vaccineimpact.orderlyweb.db.JooqContext
// import org.vaccineimpact.orderlyweb.db.Tables.*
// import org.vaccineimpact.orderlyweb.db.fromJoinPath
// import org.vaccineimpact.orderlyweb.db.joinPath
// import org.vaccineimpact.orderlyweb.models.FilePurpose
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import org.vaccineimpact.orderlyweb.test_helpers.insertReportWithPublishedAndUnpublishedVersions
// import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
// import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.tests.integration_tests.WebPermissionChecker
// import org.vaccineimpact.orderlyweb.controllers.web.ReportController

class ReportLatestTests : IntegrationTest()
{
    // private fun createSut(isReviewer: Boolean = false): ReportController
    // {
    //     // return OrderlyReportRepository(isReviewer, true, listOf())
    //     return ReportController()
    // }

    @Test
    fun `reviewer can get unpublished latest unpublished version`()
    {
        val reportName = "test"
        insertReportWithPublishedAndUnpublishedVersions(reportName)

        // val sut = createSut(isReviewer = true)

        // val result = sut.getReportVersion("test", "version1")

        val url = "/report/$reportName/latest"
        val permissions = setOf(ReifiedPermission("reports.review", Scope.Global()))
        val checker = WebPermissionChecker(url, permissions)
        val response = checker.requestWithPermissions(permissions)
        Assertions.assertThat(response.statusCode).isEqualTo("expected")

    }
}