package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.junit.Test

class GitTests: IntegrationTest() {

    @Test
    fun `gets git status`()
    {
        val response = requestHelper.get("/reports/git/status/")

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitStatus")
    }

    @Test
    fun `pulls`()
    {
        val response = requestHelper.post("/reports/git/pull/", mapOf())

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitPull")
    }

    @Test
    fun `fetches`()
    {
        val response = requestHelper.post("/reports/git/fetch/", mapOf()    )

        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitFetch")
    }
}