package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.addMembers
import org.vaccineimpact.orderlyweb.tests.createGroup
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class RolesTests : IntegrationTest()
{
    @Test
    fun `only user managers can get global report reading groups`()
    {
        val url = "/roles/report-readers/"

        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json)
    }

    @Test
    fun `can get global report reading groups`()
    {
        createGroup("Funder", ReifiedPermission("reports.read", Scope.Global()))
        addMembers("Funder", "funder.a@example.com", "funder.b@example.com")

        val url = "/roles/report-readers/"
        val response = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("users.manage", Scope.Global())),
                ContentTypes.json)

        JSONValidator.validateAgainstSchema(response.text, "UserGroups")
    }

    @Test
    fun `only user managers can get all roles`()
    {
        val url = "/roles/"

        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json)
    }

    @Test
    fun `can get all roles`()
    {
        createGroup("Funder", ReifiedPermission("reports.read", Scope.Global()))
        addMembers("Funder", "funder.a@example.com", "funder.b@example.com")
        createGroup("Reviewer", ReifiedPermission("reports.review", Scope.Global()))

        val url = "/roles/"
        val response = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("users.manage", Scope.Global())),
                ContentTypes.json)

        JSONValidator.validateAgainstSchema(response.text, "UserGroups")
    }

    @Test
    fun `can get scoped report reading groups`()
    {
        createGroup("Funder", ReifiedPermission("reports.read", Scope.Specific("report", "minimal")))
        addMembers("Funder", "funder.a@example.com", "funder.b@example.com")

        val url = "/roles/report-readers/minimal/"
        val response = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("users.manage", Scope.Global()),
                        ReifiedPermission("reports.read", Scope.Global())),
                ContentTypes.json)

        JSONValidator.validateAgainstSchema(response.text, "UserGroups")
    }

    @Test
    fun `only user managers can get all role names`()
    {
        val url = "/typeahead/roles/"

        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json)
    }
}