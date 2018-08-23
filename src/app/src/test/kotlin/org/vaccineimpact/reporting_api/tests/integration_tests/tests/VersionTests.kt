package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables
import org.vaccineimpact.reporting_api.security.InternalUser
import org.vaccineimpact.reporting_api.tests.insertReport

class VersionTests : IntegrationTest()
{
    @Test
    fun `publishes report`()
    {
        val unpublishedVersion = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.ORDERLY.ID)
                    .from(Tables.ORDERLY)
                    .where(Tables.ORDERLY.NAME.eq("minimal"))
                    .and(Tables.ORDERLY.PUBLISHED.eq(false))
                    .fetchInto(String::class.java)
                    .first()
        }

        val response = requestHelper.post("/reports/minimal/versions/$unpublishedVersion/publish/", mapOf(),
                user = requestHelper.fakeReviewer)
        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Publish")
        val data = JSONValidator.getData(response.text).asBoolean()
        assertThat(data).isEqualTo(true)
    }

    @Test
    fun `unpublishes report`()
    {
        val version = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.ORDERLY.ID)
                    .from(Tables.ORDERLY)
                    .where(Tables.ORDERLY.NAME.eq("minimal"))
                    .fetchInto(String::class.java)
                    .first()
        }

        // first lets make sure its published
        requestHelper.post("/reports/minimal/versions/$version/publish/", mapOf(), user = requestHelper.fakeReviewer)

        // now unpublish
        val response = requestHelper.post("/reports/minimal/versions/$version/publish/?value=false", mapOf(),
                user = requestHelper.fakeReviewer)

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Publish")
        val data = JSONValidator.getData(response.text).asBoolean()
        assertThat(data).isEqualTo(false)
    }

    @Test
    fun `can get all versions with global report reading permissions`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/versions/", user = requestHelper.fakeGlobalReportReader)

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Reports")
    }

    @Test
    fun `can get all versions with report reading permissions`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/versions/", user = InternalUser("testusername", "user", "*/can-login,report:testname/reports.read"))

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Reports")
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
    fun `gets zip file with access token`()
    {
        insertReport("testname", "testversion")

        val url = "/reports/testname/versions/testversion/all/"
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.getNoAuth("$url?access_token=$token", contentType = ContentTypes.zip)

        assertSuccessful(response)
        assertThat(response.headers["content-type"]).isEqualTo("application/zip")
        assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=testname/testversion.zip")
    }

    @Test
    fun `gets zip file with scoped permissions`()
    {
        insertReport("testname", "testversion")

        val response = requestHelper.get("/reports/testname/versions/testversion/all/", contentType = ContentTypes.zip,
                user = InternalUser("testusername", "user", "*/can-login,report:testname/reports.read"))

        assertSuccessful(response)
        assertThat(response.headers["content-type"]).isEqualTo("application/zip")
        assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=testname/testversion.zip")
    }


    @Test
    fun `gets zip file with bearer token`()
    {
        insertReport("testname", "testversion")

        val token = requestHelper.generateOnetimeToken("")
        val response = requestHelper.get("/reports/testname/versions/testversion/all/?access_token=$token", contentType = ContentTypes.zip)

        assertSuccessful(response)
        assertThat(response.headers["content-type"]).isEqualTo("application/zip")
        assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=testname/testversion.zip")
    }

    @Test
    fun `get zip returns 401 if access token is missing`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.getNoAuth("/reports/testname/versions/testversion/all", contentType = ContentTypes.zip)

        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateMultipleAuthErrors(response.text)
    }

    @Test
    fun `get zip returns 403 if report not in scoped report reading permissions`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/testversion/all",
                contentType = ContentTypes.zip, user = fakeReportReader("badreportnamer"))

        Assertions.assertThat(response.statusCode).isEqualTo(403)
        JSONValidator.validateError(response.text, "forbidden",
                "You do not have sufficient permissions to access this resource. Missing these permissions: report:testname/reports.read")

    }

}