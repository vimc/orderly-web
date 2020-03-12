package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_TAG
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_VERSION_TAG
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReportTags
import org.vaccineimpact.orderlyweb.test_helpers.insertVersionTags
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class TagTests : IntegrationTest()
{
    private val requiredPermissions = setOf(ReifiedPermission("reports.review", Scope.Global()))

    @Test
    fun `report reviewers can tag reports`()
    {
        val url = "/report/minimal/tags"
        val result = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tags" to listOf("test-tag", "another-tag")))
        assertThat(result.text).isEqualTo("OK")
        val tags = getReportTags("minimal")
        assertThat(tags).containsExactly("another-tag", "test-tag")
    }

    @Test
    fun `only report reviewers can tag reports`()
    {
        val url = "/report/minimal/tags"
        assertWebUrlSecured(url, requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tags" to listOf("test-tag", "another-tag")))
    }

    @Test
    fun `report reviewers can tag version`()
    {
        val (report, id) = getAnyReportIds()
        val url = "/report/$report/version/$id/tags"
        val result = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tags" to listOf("test-tag")))
        assertThat(result.text).isEqualTo("OK")
        val tags = getVersionTags(id)
        assertThat(tags.first()).isEqualTo("test-tag")
        assertThat(tags).containsExactly("test-tag")
    }

    @Test
    fun `only report reviewers can tag versions`()
    {
        val (report, id) = getAnyReportIds()
        val url = "/report/$report/version/$id/tags"
        assertWebUrlSecured(url, requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tags" to listOf("test-tag")))
    }

    @Test
    fun `report reviewers can delete report tags`()
    {
        insertReportTags("minimal", "test-tag")
        val url = "/report/minimal/tags/test-tag"
        val result = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.delete)
        assertThat(result.text).isEqualTo("OK")
        val tags = getReportTags("minimal")
        assertThat(tags.count()).isEqualTo(0)
    }

    @Test
    fun `only report reviewers can delete report tags`()
    {
        val url = "/report/minimal/tags/test-tag"
        assertWebUrlSecured(url, requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.delete)
    }

    @Test
    fun `report reviewers can delete version tags`()
    {
        val (report, id) = getAnyReportIds()
        insertVersionTags(id, "test-tag")
        val url = "/report/$report/version/$id/tags/test-tag"
        val result = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.delete)
        assertThat(result.text).isEqualTo("OK")
        val tags = getVersionTags(id)
        assertThat(tags.count()).isEqualTo(0)
    }

    @Test
    fun `only report reviewers can delete version tags`()
    {
        val (report, id) = getAnyReportIds()
        val url = "/report/$report/version/$id/tags/test-tag"
        assertWebUrlSecured(url, requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.delete)
    }

    private fun getReportTags(reportName: String): List<String>
    {
        JooqContext().use {
            return it.dsl.select(ORDERLYWEB_REPORT_TAG.TAG)
                    .from(ORDERLYWEB_REPORT_TAG)
                    .where(ORDERLYWEB_REPORT_TAG.REPORT.eq(reportName))
                    .fetchInto(String::class.java)
        }
    }

    private fun getVersionTags(versionId: String): List<String>
    {
        JooqContext().use {
            return it.dsl.select(ORDERLYWEB_REPORT_VERSION_TAG.TAG)
                    .from(ORDERLYWEB_REPORT_VERSION_TAG)
                    .where(ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION.eq(versionId))
                    .fetchInto(String::class.java)
        }
    }

    private fun getAnyReportIds(): Pair<String, String>
    {
        val report = JooqContext().use {

            it.dsl.select(Tables.REPORT_VERSION.REPORT, Tables.REPORT_VERSION.ID)
                    .from(Tables.REPORT_VERSION)
                    .where(Tables.REPORT_VERSION.PUBLISHED.eq(true))
                    .fetchAny()
        }

        return Pair(report[Tables.REPORT_VERSION.REPORT], report[Tables.REPORT_VERSION.ID])
    }
}