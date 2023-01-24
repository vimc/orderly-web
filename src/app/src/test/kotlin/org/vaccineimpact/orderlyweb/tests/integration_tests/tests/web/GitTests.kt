package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class GitTests : IntegrationTest()
{
    @Test
    fun `only report runners can get commits`()
    {
        val url = "/git/branch/master/commits/"
        assertWebUrlSecured(url, setOf(ReifiedPermission("reports.run", Scope.Global())),
                method = HttpMethod.get, contentType = ContentTypes.json)
    }

    @Test
    fun `can get commits`()
    {
        val url = "/git/branch/master/commits/"

        val response = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("reports.run", Scope.Global())),
                method = HttpMethod.get,
                contentType = ContentTypes.json)

        assertSuccessful(response)
    //    assertJsonContentType(response)
        JSONValidator.validateAgainstOrderlySchema(response.text, "GitCommits")
    }
}
