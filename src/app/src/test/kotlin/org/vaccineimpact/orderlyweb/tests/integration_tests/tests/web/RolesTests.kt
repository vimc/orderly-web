package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.addMembers
import org.vaccineimpact.orderlyweb.tests.createGroup
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

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

    @Test
    fun `only user managers can add new roles`()
    {
        val url = "/roles/"

        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json, method = HttpMethod.post, postData = mapOf("name" to "NEWGROUP"))
    }
    @Test
    fun `only user managers can add permission`()
    {
        createGroup("Funder", ReifiedPermission("reports.read", Scope.Global()))
        val url = "/roles/Funder/permissions/"

        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json, method = HttpMethod.post, postData = mapOf("name" to "users.manage"))
    }

    @Test
    fun `only user managers can remove permission`()
    {
        createGroup("Funder", ReifiedPermission("reports.read", Scope.Global()))
        val url = "/roles/Funder/permissions/reports.read/"

        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json, method = HttpMethod.delete, postData = mapOf("name" to "users.manage"))
    }

    @Test
    fun `can remove global permission`()
    {
        createGroup("Funder", ReifiedPermission("reports.read", Scope.Global()))

        var perms = OrderlyAuthorizationRepository().getPermissionsForGroup("Funder")
        assertThat(perms.count()).isEqualTo(1)

        val url = "/roles/Funder/permissions/reports.read/"

        webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("users.manage", Scope.Global())),
                method = HttpMethod.delete,
                contentType = ContentTypes.json)

        perms = OrderlyAuthorizationRepository().getPermissionsForGroup("Funder")
        assertThat(perms.count()).isEqualTo(0)
    }

    @Test
    fun `can remove specific permission`()
    {
        createGroup("Funder", ReifiedPermission("reports.read", Scope.Specific("report", "minimal")))

        var perms = OrderlyAuthorizationRepository().getPermissionsForGroup("Funder")
        assertThat(perms.count()).isEqualTo(1)

        val url = "/roles/Funder/permissions/reports.read/?scopePrefix=report&scopeId=minimal"

        webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("users.manage", Scope.Global())),
                method = HttpMethod.delete,
                contentType = ContentTypes.json)

        perms = OrderlyAuthorizationRepository().getPermissionsForGroup("Funder")
        assertThat(perms.count()).isEqualTo(0)
    }

    @Test
    fun `can add permission`()
    {
        createGroup("Funder", ReifiedPermission("reports.read", Scope.Global()))

        var perms = OrderlyAuthorizationRepository().getPermissionsForGroup("Funder")
        assertThat(perms.count()).isEqualTo(1)

        val url = "/roles/Funder/permissions/"

        webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("users.manage", Scope.Global())),
                method = HttpMethod.post, postData = mapOf("name" to "users.manage"),
                contentType = ContentTypes.json)

        perms = OrderlyAuthorizationRepository().getPermissionsForGroup("Funder")
        assertThat(perms.count()).isEqualTo(2)
    }

}