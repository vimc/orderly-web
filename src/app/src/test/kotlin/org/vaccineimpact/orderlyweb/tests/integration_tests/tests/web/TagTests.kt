package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class TagTests : IntegrationTest()
{
    private val requiredPermissions = setOf(ReifiedPermission("reports.review", Scope.Global()))

    @Test
    fun `report reviewers can tag reports`()
    {
        insertReport("r1", "v1")
        val url = "/report/r1/tag"
        val result = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tag" to "test-tag"))
        assertThat(result.text).isEqualTo("OK")
        assertReportTagExists("r1", "test-tag")
    }

    @Test
    fun `only report reviewers can tag reports`()
    {
        insertReport("r1", "v1")
        val url = "/report/r1/tag"
        assertWebUrlSecured(url, requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tag" to "test-tag"))
    }

    @Test
    fun `report reviewers can tag version`()
    {
        insertReport("r1", "v1")
        val url = "/report/r1/version/v1/tag"
        val result = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tag" to "test-tag"))
        assertThat(result.text).isEqualTo("OK")
        assertVersionTagExists("v1", "test-tag")
    }

    @Test
    fun `only report reviewers can tag versions`()
    {
        insertReport("r1", "v1")
        val url = "/report/r1/version/v1/tag"
        assertWebUrlSecured(url, requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tag" to "test-tag"))
    }

    private fun assertReportTagExists(reportName: String, tag: String) {
        val tags = JooqContext().use {
            it.dsl.select(Tables.ORDERLYWEB_REPORT_TAG.TAG)
                    .from(Tables.ORDERLYWEB_REPORT_TAG)
                    .where(Tables.ORDERLYWEB_REPORT_TAG.REPORT.eq(reportName))
                    .fetchInto(String::class.java)
        }
        assertThat(tags.first()).isEqualTo(tag)
    }

    private fun assertVersionTagExists(versionId: String, tag: String) {
        val tags = JooqContext().use {
            it.dsl.select(Tables.ORDERLYWEB_REPORT_VERSION_TAG.TAG)
                    .from(Tables.ORDERLYWEB_REPORT_VERSION_TAG)
                    .where(Tables.ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION.eq(versionId))
                    .fetchInto(String::class.java)
        }
        assertThat(tags.first()).isEqualTo(tag)
    }
}