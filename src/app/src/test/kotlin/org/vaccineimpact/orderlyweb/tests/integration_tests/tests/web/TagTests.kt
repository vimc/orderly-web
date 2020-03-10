package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class TagTests : IntegrationTest()
{
    private val requiredPermissions =  setOf(ReifiedPermission("reports.review", Scope.Global()))

    fun getTestReportIds(): Pair<String, String>
    {
        return JooqContext().use {
            val data = it.dsl.select(Tables.REPORT_VERSION.REPORT, Tables.REPORT_VERSION.ID)
                    .from(Tables.REPORT_VERSION)
                    .fetchAny()
            Pair(data[Tables.REPORT_VERSION.REPORT], data[Tables.REPORT_VERSION.ID])
        }
    }

    @Test
    fun `report reviewers can tag reports`()
    {
        val url = "/report/minimal/tag/"
        val result = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tag" to "test-tag"))
        assertThat(result.text).isEqualTo("OK")
    }

    @Test
    fun `only report reviewers can tag reports`()
    {
        val url = "/report/minimal/tag/"
        assertWebUrlSecured(url, requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tag" to "test-tag"))
    }

    @Test
    fun `report reviewers can tag version`()
    {
        val (report, version) = getTestReportIds()
        val url = "/report/$report/version/$version/tag"
        val result = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tag" to "test-tag"))
        assertThat(result.text).isEqualTo("OK")
    }

    @Test
    fun `only report reviewers can tag versions`()
    {
        val (report, version) = getTestReportIds()
        val url = "/report/$report/version/$version/tag"
        assertWebUrlSecured(url, requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tag" to "test-tag"))
    }
}