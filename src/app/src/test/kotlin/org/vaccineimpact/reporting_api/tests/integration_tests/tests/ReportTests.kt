package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.db.Tables
import org.vaccineimpact.reporting_api.tests.insertReport

class ReportTests : IntegrationTest()
{

    @Test
    fun `can get reports`()
    {
        val response = requestHelper.get("/reports")

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Reports")
    }


    @Test
    fun `runs report`()
    {
        val response = requestHelper.post("/reports/minimal/run/", mapOf(), reviewer = true)

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Run")
    }

    @Test
    fun `gets report status`()
    {
        val response = requestHelper.get("/reports/agronomic_seahorse/status/", reviewer = true)
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Status")
    }

    @Test
    fun `publishes report`()
    {
        val unpublishedVersion = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.ORDERLY.ID)
                    .from(Tables.ORDERLY)
                    .where(Tables.ORDERLY.NAME.eq("example"))
                    .and(Tables.ORDERLY.PUBLISHED.eq(false))
                    .fetchInto(String::class.java)
                    .first()
        }

        val response = requestHelper.post("/reports/minimal/$unpublishedVersion/publish/", mapOf(), reviewer = true)
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Publish")
        val data = JSONValidator.getData(response.text).asBoolean()
        assertThat(data).isEqualTo(true)
    }

    @Test
    fun `unpublishes report`()
    {
        val unpublishedVersion = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.ORDERLY.ID)
                    .from(Tables.ORDERLY)
                    .where(Tables.ORDERLY.NAME.eq("example"))
                    .and(Tables.ORDERLY.PUBLISHED.eq(false))
                    .fetchInto(String::class.java)
                    .first()
        }

        requestHelper.post("/reports/minimal/$unpublishedVersion/publish/", mapOf(), reviewer = true)

        val response = requestHelper.post("/reports/minimal/$unpublishedVersion/publish/?value=false", mapOf(), reviewer = true)
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Publish")
        val data = JSONValidator.getData(response.text).asBoolean()
        assertThat(data).isEqualTo(false)
    }

    @Test
    fun `can get report versions by name`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname")

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Report")
    }

    @Test
    fun `gets 404 if report name doesnt exist`()
    {
        val fakeName = "hjagyugs"
        val response = requestHelper.get("/reports/$fakeName")

        assertJsonContentType(response)
        assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report", "Unknown report : '$fakeName'")
    }

    @Test
    fun `can get report by name and version`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/testversion")
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Version")
    }


    @Test
    fun `reviewer can get unpublished report by name and version`()
    {
        insertReport("testname", "testversion", published = false)
        val response = requestHelper.get("/reports/testname/testversion", reviewer = true)

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Version")
    }

    @Test
    fun `gets 404 if report version doesnt exist`()
    {
        val fakeVersion = "hf647rhj"
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/$fakeVersion")

        assertJsonContentType(response)
        assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report-version", "Unknown report-version : 'testname-$fakeVersion'")
    }


    @Test
    fun `gets zip file with access token`()
    {
        insertReport("testname", "testversion")

        val url = "/reports/testname/testversion/all/"
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.getNoAuth("$url?access_token=$token", contentType = ContentTypes.zip)

        assertSuccessful(response)
        assertThat(response.headers["content-type"]).isEqualTo("application/zip")
        assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=testname/testversion.zip")
    }

    @Test
    fun `gets zip file with bearer token`()
    {
        insertReport("testname", "testversion")

        val token = requestHelper.generateOnetimeToken("")
        val response = requestHelper.get("/reports/testname/testversion/all/?access_token=$token", contentType = ContentTypes.zip)

        assertSuccessful(response)
        assertThat(response.headers["content-type"]).isEqualTo("application/zip")
        assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=testname/testversion.zip")
    }

    @Test
    fun `returns 401 if access token is missing`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.getNoAuth("/reports/testname/testversion/all", contentType = ContentTypes.zip)

        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateMultipleAuthErrors(response.text)
    }

}