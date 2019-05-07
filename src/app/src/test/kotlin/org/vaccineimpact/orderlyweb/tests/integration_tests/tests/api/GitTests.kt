package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.junit.Test
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class GitTests : IntegrationTest()
{
    @Test
    fun `gets git status`()
    {
        val response = requestHelper.get("/reports/git/status/", userEmail = fakeGlobalReportReviewer())

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitStatus")
    }

    @Test
    fun `pulls`()
    {
        val response = requestHelper.post("/reports/git/pull/", mapOf(), userEmail = fakeGlobalReportReviewer())

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitPull")
    }

    @Test
    fun `fetches`()
    {
        val response = requestHelper.post("/reports/git/fetch/", mapOf(),  userEmail = fakeGlobalReportReviewer())

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitFetch")
    }
}