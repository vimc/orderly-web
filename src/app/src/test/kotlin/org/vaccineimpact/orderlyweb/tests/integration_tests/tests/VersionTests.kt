package org.vaccineimpact.orderlyweb.tests.integration_tests.tests

import com.fasterxml.jackson.databind.node.ArrayNode
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.Tables.REPORT_VERSION
import org.vaccineimpact.orderlyweb.security.InternalUser
import org.vaccineimpact.orderlyweb.tests.insertReport

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class VersionTests : IntegrationTest()
{
    @Test // method name prefixed with A so runs first
    fun `A publishes report`()
    {
        val unpublishedVersion = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.REPORT_VERSION.ID, REPORT_VERSION.REPORT)
                    .from(Tables.REPORT_VERSION)
                    .fetchAny()
        }

        val versionId = unpublishedVersion[REPORT_VERSION.ID]
        val reportName = unpublishedVersion[REPORT_VERSION.REPORT]

        val response = requestHelper.post("/reports/$reportName/versions/$versionId/publish/", mapOf(),
                user = requestHelper.fakeReviewer)
        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Publish")
        val data = JSONValidator.getData(response.text).asBoolean()
        assertThat(data).isEqualTo(true)

        val publishStatus = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.REPORT_VERSION.PUBLISHED)
                    .from(Tables.REPORT_VERSION)
                    .where(Tables.REPORT_VERSION.REPORT.eq(reportName))
                    .and(Tables.REPORT_VERSION.ID.eq(versionId))
                    .fetchInto(Boolean::class.java)
                    .first()
        }

        assertThat(publishStatus).isTrue()
    }

    @Test // method name prefixed with B so runs first
    fun `B unpublishes report`()
    {
        val publishedVersion = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.REPORT_VERSION.ID, REPORT_VERSION.REPORT)
                    .from(Tables.REPORT_VERSION)
                    .where(Tables.REPORT_VERSION.PUBLISHED.eq(true))
                    .fetchAny()
        }

        val versionId = publishedVersion[REPORT_VERSION.ID]
        val reportName = publishedVersion[REPORT_VERSION.REPORT]

        // now unpublish
        val response = requestHelper.post("/reports/$reportName/versions/$versionId/publish/?value=false", mapOf(),
                user = requestHelper.fakeReviewer)

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Publish")
        val data = JSONValidator.getData(response.text).asBoolean()
        assertThat(data).isEqualTo(false)

        val publishStatus = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.REPORT_VERSION.PUBLISHED)
                    .from(Tables.REPORT_VERSION)
                    .where(Tables.REPORT_VERSION.REPORT.eq(reportName))
                    .and(Tables.REPORT_VERSION.ID.eq(versionId))
                    .fetchInto(Boolean::class.java)
                    .first()
        }

        assertThat(publishStatus).isFalse()
    }

    @Test
    fun `can get all versions with global report reading permissions`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/versions/", user = requestHelper.fakeGlobalReportReader)

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Reports")
        val data = JSONValidator.getData(response.text)

        // there are 6 report versions in the test data set, plus the one we added above
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
        insertReport("testname", "testversion")
        val response = requestHelper.get("/versions/",
                user = InternalUser("testusername", "user", "*/can-login,report:testname/reports.read"))

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Reports")
        val data = JSONValidator.getData(response.text)

        assertThat(data.count()).isEqualTo(1)
        assertThat(data[0].get("name").asText()).isEqualTo("testname")
    }


    @Test
    fun `can get report versions by name with global report reading permissions`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname", user = requestHelper.fakeGlobalReportReader)

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Versions")
    }

    @Test
    fun `can get report versions by name with specific report reading permissions`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname", user = InternalUser("testusername", "user", "*/can-login,report:testname/reports.read"))

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Versions")
    }

    @Test
    fun `get report versions throws 403 if user not authorized to read report`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname", user = fakeReportReader("badreportname"))

        assertUnauthorized(response, "testname")
    }

    @Test
    fun `can get report by name and version with global permissionss`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/testversion",
                user = requestHelper.fakeGlobalReportReader)
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Version")
    }

    @Test
    fun `can get report by name and version with scoped permission`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/testversion",
                user = InternalUser("testusername", "user", "*/can-login,report:testname/reports.read"))
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Version")
    }

    @Test
    fun `get by name and version returns 403 if report not in scoped permissions`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/testversion",
                user = fakeReportReader("badreportname"))
        assertUnauthorized(response, "testname")
    }

    @Test
    fun `reviewer can get unpublished report by name and version`()
    {
        insertReport("testname", "testversion", published = false)
        val response = requestHelper.get("/reports/testname/versions/testversion", user = requestHelper.fakeReviewer)

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Version")
    }

    @Test
    fun `gets 404 if report version doesnt exist`()
    {
        val fakeVersion = "hf647rhj"
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/$fakeVersion")

        assertJsonContentType(response)
        assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report-version", "Unknown report-version : 'testname-$fakeVersion'")
    }

    @Test
    fun `can get version changelog by name and version`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/testversion/changelog/",
                user = requestHelper.fakeReviewer)
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Changelog")
    }

    @Test
    fun `can get empty version changelog by name and version`()
    {
        insertReport("testname", "testversion", changelog = listOf())
        val response = requestHelper.get("/reports/testname/versions/testversion/changelog/",
                user = requestHelper.fakeReviewer)
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Changelog")
        val count = (JSONValidator.getData(response.text) as ArrayNode).size()
        Assertions.assertThat(count).isEqualTo(0)
    }

    @Test
    fun `can get version changelog by name and version if global reader permissions only`()
    {
        //reader now has permission to get public changelog items for published reports which they have perms to read
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/testversion/changelog/",
                user = requestHelper.fakeGlobalReportReader)
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Changelog")
    }

    @Test
    fun `get changelog returns 404 if version does not belong to report`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/notatestversion/changelog",
                user = requestHelper.fakeReviewer)

        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report-version",
                "Unknown report-version")
    }

    @Test
    fun `get changelog returns 404 if version is not published and user has reader permission only`()
    {
        insertReport("testname", "testversion", published = false)
        val response = requestHelper.get("/reports/testname/versions/testversion/changelog",
                user = requestHelper.fakeGlobalReportReader)

        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report-version",
                "Unknown report-version")
    }

}