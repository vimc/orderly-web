package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import com.fasterxml.jackson.databind.node.ArrayNode
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_VERSION
import org.vaccineimpact.orderlyweb.db.Tables.REPORT_VERSION
import org.vaccineimpact.orderlyweb.db.fromJoinPath
import org.vaccineimpact.orderlyweb.db.joinPath
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.InsertableChangelog
import org.vaccineimpact.orderlyweb.tests.insertChangelog
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class VersionTests : IntegrationTest()
{
    @Test // method name prefixed with A so runs first
    fun `A publishes report`()
    {
        val unpublishedVersion = JooqContext().use {

            it.dsl.select(REPORT_VERSION.ID, REPORT_VERSION.REPORT)
                    .from(REPORT_VERSION)
                    .fetchAny()
        }

        val versionId = unpublishedVersion[REPORT_VERSION.ID]
        val reportName = unpublishedVersion[REPORT_VERSION.REPORT]

        val response = apiRequestHelper.post("/reports/$reportName/versions/$versionId/publish/", mapOf(),
                userEmail = fakeGlobalReportReviewer())
        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Publish")
        val data = JSONValidator.getData(response.text).asBoolean()
        assertThat(data).isEqualTo(true)

        val publishStatus = JooqContext().use {

            it.dsl.select(ORDERLYWEB_REPORT_VERSION.PUBLISHED)
                    .fromJoinPath(REPORT_VERSION, ORDERLYWEB_REPORT_VERSION)
                    .where(REPORT_VERSION.REPORT.eq(reportName))
                    .and(REPORT_VERSION.ID.eq(versionId))
                    .fetchInto(Boolean::class.java)
                    .first()
        }

        assertThat(publishStatus).isTrue()
    }

    @Test // method name prefixed with B so runs second
    fun `B unpublishes report`()
    {
        val publishedVersion = JooqContext().use {

            it.dsl.select(REPORT_VERSION.ID, REPORT_VERSION.REPORT)
                    .fromJoinPath(REPORT_VERSION, ORDERLYWEB_REPORT_VERSION)
                    .where(ORDERLYWEB_REPORT_VERSION.PUBLISHED.eq(true))
                    .fetchAny()
        }

        val versionId = publishedVersion[REPORT_VERSION.ID]
        val reportName = publishedVersion[REPORT_VERSION.REPORT]

        // now unpublish
        val response = apiRequestHelper.post("/reports/$reportName/versions/$versionId/publish/?value=false", mapOf(),
                userEmail = fakeGlobalReportReviewer())

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Publish")
        val data = JSONValidator.getData(response.text).asBoolean()
        assertThat(data).isEqualTo(false)

        val publishStatus = JooqContext().use {

            it.dsl.select(ORDERLYWEB_REPORT_VERSION.PUBLISHED)
                    .fromJoinPath(REPORT_VERSION, ORDERLYWEB_REPORT_VERSION)
                    .where(REPORT_VERSION.REPORT.eq(reportName))
                    .and(REPORT_VERSION.ID.eq(versionId))
                    .fetchInto(Boolean::class.java)
                    .first()
        }

        assertThat(publishStatus).isFalse()
    }

    @Test // method name prefixed with C so runs third
    fun `C only report reviewers can publish reports`()
    {
        val version = JooqContext().use {

            it.dsl.select(REPORT_VERSION.ID, REPORT_VERSION.REPORT)
                    .from(REPORT_VERSION)
                    .fetchAny()
        }

        val versionId = version[REPORT_VERSION.ID]
        val reportName = version[REPORT_VERSION.REPORT]

        val url = "/reports/$reportName/versions/$versionId/publish/"

        assertAPIUrlSecured(url,
                setOf(ReifiedPermission("reports.review", Scope.Global())),
                method = HttpMethod.post,
                contentType = ContentTypes.json)
    }

    @Test
    fun `can get all versions with global report reading permissions`()
    {
        insertReport("testname", "testversion")
        val response = apiRequestHelper.get("/versions/", userEmail = fakeGlobalReportReader())

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Reports")
        val data = JSONValidator.getData(response.text)

        // there are 7 report versions in the test data set, plus the one we added above
        assertThat(data.count()).isEqualTo(8)
        val names = data.map {
            it.get("name").asText()
        }
        assertThat(names.sorted())
                .containsExactlyElementsOf(listOf(
                        "html",
                        "minimal",
                        "multi-artefact",
                        "multifile-artefact",
                        "other",
                        "other",
                        "testname",
                        "use_resource"
                ))
    }

    @Test
    fun `can get all versions with specific report reading permissions`()
    {
        val response = apiRequestHelper.get("/versions/",
                userEmail = fakeReportReader("html", addReport = false))

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Reports")
        val data = JSONValidator.getData(response.text)

        assertThat(data.count()).isEqualTo(1)
        assertThat(data[0].get("name").asText()).isEqualTo("html")
    }

    @Test
    fun `can get report version ids`()
    {
        val response = apiRequestHelper.get("/reports/minimal")

        assertJsonContentType(response)
        assertThat(response.statusCode).isEqualTo(200)
        JSONValidator.validateAgainstSchema(response.text, "VersionIds")
    }

    @Test
    fun `only report readers can get report version ids`()
    {
        val url = "/reports/minimal"
        assertAPIUrlSecured(url, setOf(ReifiedPermission("reports.read", Scope.Global())), ContentTypes.json)
    }

    @Test
    fun `can get report by name and version`()
    {
        insertReport("testname", "testversion")
        val response = apiRequestHelper.get("/reports/testname/versions/testversion",
                userEmail = fakeGlobalReportReader())
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "VersionDetails")
    }

    @Test
    fun `only report readers can get report by name and version`()
    {
        insertReport("testname", "testversion")
        val url = "/reports/testname/versions/testversion"
        assertAPIUrlSecured(url,
                setOf(ReifiedPermission("reports.read", Scope.Specific("report", "testname"))),
                contentType = ContentTypes.json)
    }

    @Test
    fun `reviewer can get unpublished report by name and version`()
    {
        insertReport("testname", "testversion", published = false)
        val response = apiRequestHelper.get("/reports/testname/versions/testversion",
                userEmail = fakeGlobalReportReviewer())

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "VersionDetails")
    }

    @Test
    fun `non report reviewers cannot get unpublished report by name and version`()
    {
        insertReport("testname", "testversion", published = false)
        val response = apiRequestHelper.get("/reports/testname/versions/testversion",
                userEmail = fakeGlobalReportReader())
        assertJsonContentType(response)
        // can't use the permission checker here because some security checking
        // happens inside the controller logic rather than the routing logic and
        // the result of insufficient permissions is a 404 rather than a 403
        assertThat(response.statusCode).isEqualTo(404)
    }

    @Test
    fun `gets 404 if report version doesnt exist`()
    {
        val fakeVersion = "hf647rhj"
        insertReport("testname", "testversion")

        val response = apiRequestHelper.get("/reports/testname/versions/$fakeVersion")

        assertJsonContentType(response)
        assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report-version", "Unknown report-version : 'testname-$fakeVersion'")
    }

    @Test
    fun `can get version changelog by name and version`()
    {
        insertReport("testname", "testversion")
        insertChangelog(listOf(
                InsertableChangelog(
                        "id1",
                        "testversion",
                        "internal",
                        "did something awful",
                        false,
                        1),
                InsertableChangelog(
                        "id2",
                        "testversion",
                        "public",
                        "did something great",
                        true,
                        2)))

        val response = apiRequestHelper.get("/reports/testname/versions/testversion/changelog/",
                userEmail = fakeGlobalReportReader())
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Changelog")
    }

    @Test
    fun `can get empty version changelog by name and version`()
    {
        insertReport("testname", "testversion")
        val response = apiRequestHelper.get("/reports/testname/versions/testversion/changelog/",
                userEmail = fakeGlobalReportReader())
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Changelog")
        val count = (JSONValidator.getData(response.text) as ArrayNode).size()
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `only report readers can get version changelog by name and version`()
    {
        insertReport("testname", "testversion")
        val url = "/reports/testname/versions/testversion/changelog/"
        assertAPIUrlSecured(url,
                setOf(ReifiedPermission("reports.read", Scope.Specific("report", "testname"))),
                contentType = ContentTypes.json)
    }

    @Test
    fun `get changelog returns 404 if version does not belong to report`()
    {
        insertReport("testname", "testversion")
        insertChangelog(listOf(
                InsertableChangelog(
                        "id1",
                        "testversion",
                        "internal",
                        "did something awful",
                        false,
                        1),
                InsertableChangelog(
                        "id2",
                        "testversion",
                        "public",
                        "did something great",
                        true,
                        2)))

        val response = apiRequestHelper.get("/reports/testname/versions/notatestversion/changelog",
                userEmail = fakeGlobalReportReader())

        assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report-version",
                "Unknown report-version")
    }

    @Test
    fun `get changelog returns 404 if version is not published and user has reader permission only`()
    {
        insertReport("testname", "testversion", published = false)
        insertChangelog(listOf(
                InsertableChangelog(
                        "id1",
                        "testversion",
                        "internal",
                        "did something awful",
                        false,
                        1),
                InsertableChangelog(
                        "id2",
                        "testversion",
                        "public",
                        "did something great",
                        true,
                        2)))

        val response = apiRequestHelper.get("/reports/testname/versions/testversion/changelog",
                userEmail = fakeGlobalReportReader())

        assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report-version",
                "Unknown report-version")
    }

    @Test
    fun `can get report run metadata with access token`()
    {
        val publishedVersion = JooqContext().use {
            it.dsl.select(REPORT_VERSION.ID, REPORT_VERSION.REPORT)
                    .fromJoinPath(REPORT_VERSION, ORDERLYWEB_REPORT_VERSION)
                    .where(ORDERLYWEB_REPORT_VERSION.PUBLISHED)
                    .fetchAny()
        }
        val versionId = publishedVersion[REPORT_VERSION.ID]
        val reportName = publishedVersion[REPORT_VERSION.REPORT]
        val url = "/reports/${reportName}/versions/${versionId}/run-meta/"
        val token = apiRequestHelper.generateOnetimeToken(url)
        val response = apiRequestHelper.getNoAuth("$url?access_token=$token", ContentTypes.binarydata)

        assertSuccessful(response)
        assertThat(response.headers["content-type"]).isEqualTo("application/octet-stream")
        assertThat(response.headers["content-disposition"])
                .isEqualTo("attachment; filename=\"$reportName/$versionId/orderly_run.rds\"")
    }

    @Test
    fun `only report readers can get report run metadata`()
    {
        val publishedVersion = JooqContext().use {
            it.dsl.select(REPORT_VERSION.ID, REPORT_VERSION.REPORT)
                    .fromJoinPath(REPORT_VERSION, ORDERLYWEB_REPORT_VERSION)
                    .where(ORDERLYWEB_REPORT_VERSION.PUBLISHED)
                    .fetchAny()
        }
        val versionId = publishedVersion[REPORT_VERSION.ID]
        val reportName = publishedVersion[REPORT_VERSION.REPORT]
        val url = "/reports/${reportName}/versions/${versionId}/run-meta/"

        assertAPIUrlSecured(url,
                setOf(ReifiedPermission("reports.read", Scope.Specific("report", reportName))),
                contentType = ContentTypes.binarydata)
    }

}