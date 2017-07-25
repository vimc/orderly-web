package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.tests.insertReport

import org.assertj.core.api.Assertions.assertThat
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.Config
import java.io.File

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
    fun `gets zip file`()
    {
        insertReport("testname", "testversion")

        val token = requestHelper.generateOnetimeToken()
        val response = requestHelper.get("/reports/testname/testversion/all/?access_token=$token", contentType = ContentTypes.zip)

        assertSuccessful(response)
        assertThat(response.headers["content-type"]).isEqualTo("application/zip")
        assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=testname/testversion.zip")
    }

    @Test
    fun `returns 400 if access token is missing`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/testversion/all", contentType = ContentTypes.zip)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(400)
        JSONValidator.validateError(response.text, "invalid-token-verification", "Access token is missing")
    }

}
