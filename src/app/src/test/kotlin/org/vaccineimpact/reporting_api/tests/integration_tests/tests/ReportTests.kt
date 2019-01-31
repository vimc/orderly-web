package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions.assertThat
import com.fasterxml.jackson.databind.node.ArrayNode
import org.junit.Test
import org.vaccineimpact.reporting_api.tests.insertReport


class ReportTests : IntegrationTest()
{

    @Test
    fun `can get reports`()
    {
        val response = requestHelper.get("/reports/")

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Reports")
    }

    @Test
    fun `runs report`()
    {
        val response = requestHelper.post("/reports/minimal/run/", mapOf(), user = requestHelper.fakeReviewer)

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Run")
    }

    @Test
    fun `gets report status`()
    {
        val response = requestHelper.get("/reports/agronomic_seahorse/status/", user = requestHelper.fakeReviewer)
        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Status")
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
    fun `can get latest changelog by name`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/latest/changelog/",
                user = requestHelper.fakeReviewer)
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Changelog")
    }

    @Test
    fun `can get empty latest changelog by name`()
    {
        insertReport("testname", "testversion", changelog = listOf())
        val response = requestHelper.get("/reports/testname/latest/changelog/",
                user = requestHelper.fakeReviewer)
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Changelog")
        val count = (JSONValidator.getData(response.text) as ArrayNode).size()
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `can get latest changelog if reader permissions only`()
    {
        //This report has been published so we should be able to see it, though it has no log items
       val response = requestHelper.get("/reports/other/latest/changelog",
                user = requestHelper.fakeGlobalReportReader)

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Changelog")
        val count = (JSONValidator.getData(response.text) as ArrayNode).size()
        assertThat(count).isEqualTo(0)

    }

    @Test
    fun `get latest changelog returns 404 if report does not exist`()
    {
        val response = requestHelper.get("/reports/testname/latest/changelog",
                user = requestHelper.fakeReviewer)

        assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report",
                "Unknown report")
    }

}