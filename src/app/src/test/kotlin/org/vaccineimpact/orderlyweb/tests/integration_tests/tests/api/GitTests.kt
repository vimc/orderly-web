package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class GitTests : IntegrationTest()
{
    @Test
    fun `gets git status`()
    {
        val response = apiRequestHelper.get("/git/status/", userEmail = fakeGlobalReportReviewer())

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitStatus")
    }

    @Test
    fun `only report runners can get git status`()
    {
        val url = "/git/status/"
        assertAPIUrlSecured(url,
                setOf(ReifiedPermission("reports.run", Scope.Global())),
                contentType = ContentTypes.json)
    }

    @Test
    fun pulls()
    {
        val response = apiRequestHelper.post("/git/pull/", mapOf(), userEmail = fakeGlobalReportReviewer())

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitPull")
    }

    @Test
    fun `only report runners can pull`()
    {
        val url = "/git/pull/"
        assertAPIUrlSecured(url,
                setOf(ReifiedPermission("reports.run", Scope.Global())),
                method = HttpMethod.post,
                contentType = ContentTypes.json)
    }

    @Test
    fun fetches()
    {
        val response = apiRequestHelper.post("/git/fetch/", mapOf(),  userEmail = fakeGlobalReportReviewer())

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "GitFetch")
    }

    @Test
    fun `only report runners can fetch`()
    {
        val url = "/git/fetch/"
        assertAPIUrlSecured(url,
                setOf(ReifiedPermission("reports.run", Scope.Global())),
                method = HttpMethod.post,
                contentType = ContentTypes.json)
    }
}