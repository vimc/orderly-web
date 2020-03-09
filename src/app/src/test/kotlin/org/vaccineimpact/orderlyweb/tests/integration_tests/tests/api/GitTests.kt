package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class GitTests : IntegrationTest()
{
    @Test
    fun `gets git status`()
    {
        val response = apiRequestHelper.get("/reports/git/status/", userEmail = fakeGlobalReportReviewer())

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitStatus")
    }

    @Test
    fun `only report runners can get git status`()
    {
        val url ="/reports/git/status/"
        assertAPIUrlSecured(url, setOf(ReifiedPermission("reports.run", Scope.Global())))
    }

    @Test
    fun `pulls`()
    {
        val response = apiRequestHelper.post("/reports/git/pull/", mapOf(), userEmail = fakeGlobalReportReviewer())

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitPull")
    }

    @Test
    fun `only report runners can pull`()
    {
        val url ="/reports/git/pull/"
        assertAPIUrlSecured(url, setOf(ReifiedPermission("reports.run", Scope.Global())))
    }

    @Test
    fun `fetches`()
    {
        val response = apiRequestHelper.post("/reports/git/fetch/", mapOf(),  userEmail = fakeGlobalReportReviewer())

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitFetch")
    }

    @Test
    fun `only report runners can fetch`()
    {
        val url ="/reports/git/fetch/"
        assertAPIUrlSecured(url, setOf(ReifiedPermission("reports.run", Scope.Global())))
    }
}