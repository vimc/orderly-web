package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.addMembers
import org.vaccineimpact.orderlyweb.tests.createGroup
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class ReportTests : IntegrationTest()
{
    @Test
    fun `only report runners can run report`()
    {
        val url = "/report/minimal/actions/run"
        assertWebUrlSecured(url, setOf(ReifiedPermission("reports.run", Scope.Global())),
                method = HttpMethod.post, contentType = ContentTypes.json)
    }

    @Test
    fun `only report runners can get running report status`()
    {
        val url = "/report/minimal/actions/status/frightened_rabbit/"
        val requiredPermissions = setOf(ReifiedPermission("reports.run", Scope.Global()))
        assertWebUrlSecured(url, requiredPermissions, contentType = ContentTypes.json)
    }

    @Test
    fun `can get scoped report reading groups`()
    {
        createGroup("Funder", ReifiedPermission("reports.read", Scope.Specific("report", "minimal")))
        addMembers("Funder", "funder.a@example.com", "funder.b@example.com")

        val url = "/report/minimal/report-readers/"
        val response = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("users.manage", Scope.Global()),
                        ReifiedPermission("reports.read", Scope.Global())),
                ContentTypes.json)

        JSONValidator.validateAgainstSchema(response.text, "UserGroups")
    }
}