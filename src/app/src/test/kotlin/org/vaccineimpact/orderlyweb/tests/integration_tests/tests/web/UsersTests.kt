package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.addMembers
import org.vaccineimpact.orderlyweb.tests.createGroup
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class UsersTests : IntegrationTest()
{
    @Test
    fun `only user managers can get report readers`()
    {
        val url = "/users/report-readers/minimal"

        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json)
    }

    @Test
    fun `only user managers can associate permission`()
    {
        val url = "/user-groups/test.user%40example.com/actions/associate-permission/"

        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json, method = HttpMethod.post, postData = mapOf("action" to "add",
                "name" to "users.manage"))
    }

    @Test
    fun `only user managers can add new user groups`()
    {
        val url = "/user-groups/"

        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json, method = HttpMethod.post, postData = mapOf("name" to "NEWGROUP"))
    }

    @Test
    fun `only user managers can get user emails`()
    {
        val url = "/typeahead/emails/"

        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json)
    }

}