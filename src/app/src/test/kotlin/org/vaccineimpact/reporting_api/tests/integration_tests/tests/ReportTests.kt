package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

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

}