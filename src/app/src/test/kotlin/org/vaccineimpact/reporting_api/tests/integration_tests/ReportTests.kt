package org.vaccineimpact.reporting_api.tests.integration_tests

import org.junit.Test
import org.vaccineimpact.reporting_api.tests.insertReport

import org.assertj.core.api.Assertions.assertThat
import org.vaccineimpact.reporting_api.tests.integration_tests.helpers.RequestHelper
import org.vaccineimpact.reporting_api.tests.integration_tests.validators.JSONValidator

class ReportTests: IntegrationTest()
{
    val requestHelper = RequestHelper()
    val JSONValidator = JSONValidator()

    @Test
    fun `can get reports`()
    {
        val response = requestHelper.get("/reports")
        JSONValidator.validateAgainstSchema(response.text, "Reports")
    }

    @Test
    fun `can get report versions by name`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname")
        JSONValidator.validateAgainstSchema(response.text, "Report")
    }

    @Test
    fun `gets 404 if report name doesnt exist`()
    {
        val fakeName = "hjagyugs"
        val response = requestHelper.get("/reports/$fakeName")

        assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report", "Unknown report : '$fakeName'")
    }

    @Test
    fun `can get report by name and version`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/testversion")
        JSONValidator.validateAgainstSchema(response.text, "Version")
    }

    @Test
    fun `gets 404 if report version doesnt exist`()
    {
        val fakeVersion = "hf647rhj"
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/$fakeVersion")

        assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report-version", "Unknown report-version : 'testname-$fakeVersion'")
    }

}
