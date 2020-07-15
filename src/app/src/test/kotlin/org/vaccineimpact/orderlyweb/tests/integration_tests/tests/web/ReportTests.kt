package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.junit.Test
import org.assertj.core.api.Assertions.assertThat
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class ReportTests : IntegrationTest()
{
    @Test
    fun `only report runners can run report`()
    {
        val url = "/report/minimal/actions/run"
        assertWebUrlSecured(url, setOf(ReifiedPermission("reports.run", Scope.Global())),
                method = HttpMethod.post, contentType = ContentTypes.json)
    }

    @Test
    fun `only report runners can get running report status`()
    {
        val url = "/report/minimal/actions/status/frightened_rabbit/"
        val requiredPermissions = setOf(ReifiedPermission("reports.run", Scope.Global()))
        assertWebUrlSecured(url, requiredPermissions, contentType = ContentTypes.json)
    }

    @Test
    fun `only settings managers can set global pinned reports`()
    {
        val url = "/global-pinned-reports/"
        val requiredPermissions = setOf(ReifiedPermission("pinned-reports.manage", Scope.Global()))
        assertWebUrlSecured(url, requiredPermissions, method = HttpMethod.post, contentType = ContentTypes.json,
                postData = mapOf("reports" to listOf<String>()))
    }

    @Test
    fun `can set global pinned reports`()
    {
        val url = "/global-pinned-reports/"

        webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("pinned-reports.manage", Scope.Global())),
                method = HttpMethod.post,
                postData = mapOf("reports" to listOf("html")),
                contentType = ContentTypes.json)

        val pinnedReports = OrderlyReportRepository(true, true).getGlobalPinnedReports()
        assertThat(pinnedReports.count()).isEqualTo(1)
        assertThat(pinnedReports[0].name).isEqualTo("html")
    }

    @Test
    fun `only report reviewers can publish reports`()
    {
        val url = "/publish-reports/"
        assertWebUrlSecured(url,
                setOf(ReifiedPermission("reports.review", Scope.Global())),
                method = HttpMethod.post,
                contentType = ContentTypes.json,
                postData = mapOf("ids" to listOf("v1")))
    }

    @Test
    fun `report reviewers can publish reports`()
    {
        insertReport("report", "v1", published = false)
        insertReport("report", "v2", published = false)

        val url = "/publish-reports/"
        webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("reports.review", Scope.Global())),
                method = HttpMethod.post,
                postData = mapOf("ids" to listOf("v1", "v2")),
                contentType = ContentTypes.json)

        val repo = OrderlyReportRepository(true, true)

        assertThat(repo.getReportVersion("report", "v1").published).isTrue()
        assertThat(repo.getReportVersion("report", "v2").published).isTrue()
    }
}