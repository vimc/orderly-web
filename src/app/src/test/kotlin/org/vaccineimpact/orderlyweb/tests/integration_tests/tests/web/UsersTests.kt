package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.giveUserGroupPermission
import org.vaccineimpact.orderlyweb.tests.insertUser
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class UsersTests : IntegrationTest()
{
    @Test
    fun `only user managers can get all users`()
    {
        val url = "/users/"

        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json)
    }

    @Test
    fun `can get all users`()
    {
        insertUser("a.user", "A user")
        insertUser("b.user", "B user")

        val url = "/users/"
        val response = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("users.manage", Scope.Global())),
                ContentTypes.json)

        JSONValidator.validateAgainstSchema(response.text, "Users")
    }

    @Test
    fun `only user managers can get report readers`()
    {
        val url = "/users/report-readers/minimal"

        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json)
    }

    @Test
    fun `only user managers can add permission`()
    {
        val url = "/users/test.user%40example.com/permissions/"

        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json, method = HttpMethod.post, postData = mapOf("name" to "users.manage"))
    }

    @Test
    fun `only user managers can remove permission`()
    {
        insertUser("a.user", "A user")
        giveUserGroupPermission("a.user", "reports.read", Scope.Global())
        val url = "/users/a.user/permissions/reports.read"

        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json, method = HttpMethod.delete, postData = mapOf("name" to "users.manage"))
    }

    @Test
    fun `can remove global permission`()
    {
        insertUser("a.user", "A user")
        giveUserGroupPermission("a.user", "reports.read", Scope.Global())

        var perms = OrderlyAuthorizationRepository().getPermissionsForGroup("a.user")
        Assertions.assertThat(perms.count()).isEqualTo(1)

        val url = "/users/a.user/permissions/reports.read/"

        webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("users.manage", Scope.Global())),
                method = HttpMethod.delete,
                contentType = ContentTypes.json)

        perms = OrderlyAuthorizationRepository().getPermissionsForGroup("a.user")
        Assertions.assertThat(perms.count()).isEqualTo(0)
    }

    @Test
    fun `can remove specific permission`()
    {
        insertUser("a.user", "A user")
        giveUserGroupPermission("a.user", "reports.read", Scope.Specific("report", "minimal"))

        var perms = OrderlyAuthorizationRepository().getPermissionsForGroup("a.user")
        Assertions.assertThat(perms.count()).isEqualTo(1)

        val url = "/users/a.user/permissions/reports.read/?scopePrefix=report&scopeId=minimal"

        webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("users.manage", Scope.Global())),
                method = HttpMethod.delete,
                contentType = ContentTypes.json)

        perms = OrderlyAuthorizationRepository().getPermissionsForGroup("a.user")
        Assertions.assertThat(perms.count()).isEqualTo(0)
    }

    @Test
    fun `can add permission`()
    {
        insertUser("a.user", "A user")

        var perms = OrderlyAuthorizationRepository().getPermissionsForGroup("a.user")
        Assertions.assertThat(perms.count()).isEqualTo(0)

        val url = "/users/a.user/permissions/"

        webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("users.manage", Scope.Global())),
                method = HttpMethod.post, postData = mapOf("name" to "users.manage"),
                contentType = ContentTypes.json)

        perms = OrderlyAuthorizationRepository().getPermissionsForGroup("a.user")
        Assertions.assertThat(perms.count()).isEqualTo(1)
    }
}
