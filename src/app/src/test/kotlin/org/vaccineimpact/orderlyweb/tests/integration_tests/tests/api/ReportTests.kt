package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import com.fasterxml.jackson.databind.node.ArrayNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class ReportTests : IntegrationTest()
{

    @Test
    fun `can get reports`()
    {
        val response = apiRequestHelper.get("/reports/")

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Reports")
    }

    @Test
    fun `runs report`()
    {
        val response = apiRequestHelper.post("/reports/minimal/run/", mapOf(),
                userEmail = fakeGlobalReportReviewer())

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Run")
    }

    @Test
    fun `only report runners can run report`()
    {
        val url = "/reports/minimal/run/"
        assertAPIUrlSecured(url,
                setOf(ReifiedPermission("reports.run", Scope.Global())),
                method = HttpMethod.post,
                contentType = ContentTypes.json)
    }

    @Test
    fun `gets report status`()
    {
        val response = apiRequestHelper.get("/reports/agronomic_seahorse/status",
                userEmail = fakeGlobalReportReviewer())
        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Status")
    }

    @Test
    fun `only report runners can see status`()
    {
        val url = "/reports/agronomic_seahorse/status"
        assertAPIUrlSecured(url, setOf(ReifiedPermission("reports.run", Scope.Global())), ContentTypes.json)
    }

    @Test
    fun `gets 404 if report name doesnt exist`()
    {
        val fakeName = "hjagyugs"
        val response = apiRequestHelper.get("/reports/$fakeName")

        assertJsonContentType(response)
        assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report", "Unknown report : '$fakeName'")
    }

    @Test
    fun `can get latest changelog by name`()
    {
        insertReport("testname", "testversion")
        val response = apiRequestHelper.get("/reports/testname/latest/changelog/",
                userEmail = fakeGlobalReportReviewer())
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Changelog")
    }

    @Test
    fun `can get empty latest changelog by name`()
    {
        insertReport("testname", "testversion")
        val response = apiRequestHelper.get("/reports/testname/latest/changelog/",
                userEmail = fakeGlobalReportReviewer())
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
        val response = apiRequestHelper.get("/reports/other/latest/changelog",
                userEmail = fakeGlobalReportReviewer())

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Changelog")
        val count = (JSONValidator.getData(response.text) as ArrayNode).size()
        assertThat(count).isEqualTo(0)

    }

    @Test
    fun `get latest changelog returns 404 if report does not exist`()
    {
        val response = apiRequestHelper.get("/reports/testname/latest/changelog",
                userEmail = fakeGlobalReportReviewer())

        assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report",
                "Unknown report")
    }

}