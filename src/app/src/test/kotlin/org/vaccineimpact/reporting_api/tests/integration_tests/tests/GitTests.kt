package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.junit.Test
import org.junit.Ignore

class GitTests : IntegrationTest()
{

    @Test
    fun `gets git status`()
    {
        val response = requestHelper.get("/reports/git/status/", user = requestHelper.fakeReviewer)

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitStatus")
    }

    @Test
    fun `pulls`()
    {
        val response = requestHelper.post("/reports/git/pull/", mapOf(), user = requestHelper.fakeReviewer)

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitPull")
    }

    @Test
    fun `fetches`()
    {
        val response = requestHelper.post("/reports/git/fetch/", mapOf(),  user = requestHelper.fakeReviewer)

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitFetch")
    }
}