package org.vaccineimpact.orderlyweb.tests


import khttp.responses.Response
import org.assertj.core.api.Assertions
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.test_helpers.removePermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.RequestHelper
import spark.route.HttpMethod

abstract class PermissionChecker(protected val url: String,
                                 protected val allRequiredPermissions: Set<ReifiedPermission>,
                                 protected val contentType: String = ContentTypes.html,
                                 protected val method: HttpMethod = HttpMethod.get,
                                 protected val postData: Map<String, Any>? = null)
{

    abstract val requestHelper: RequestHelper
    abstract val unsecuredResponseCode: Int

    fun checkPermissionsAreSufficient()
    {
        checkThesePermissionsAreSufficient(allRequiredPermissions,
                "Expected successful request to $url with the given permissions")
    }

    fun checkPermissionIsRequired(
            permission: ReifiedPermission
    )
    {
        val assertionText = "Expected permission '$permission' to be required for $url"
        val limitedPermissions = allRequiredPermissions - permission
        removePermission(requestHelper.MontaguTestUser, permission.name, permission.scope.databaseScopePrefix ?: "")

        // also remove the global permission which should always be stronger than the scoped one
        removePermission(requestHelper.MontaguTestUser, permission.name, "")

        checkThesePermissionsAreInsufficient(limitedPermissions, assertionText)

        if (permission.scope is Scope.Specific)
        {
            val scope = permission.scope as Scope.Specific

            val badPermission = ReifiedPermission(permission.name, Scope.Specific(scope.databaseScopePrefix, "bad-id"))
            insertReport("bad-id", "v1")
            checkThesePermissionsAreInsufficient(limitedPermissions + badPermission, assertionText)

            val betterPermission = ReifiedPermission(permission.name, Scope.Global())
            checkThesePermissionsAreSufficient(limitedPermissions + betterPermission,
                    "Expected to be able to substitute '$betterPermission' in place of '$permission' for $url")
        }
    }

    abstract fun requestWithPermissions(permissions: Set<ReifiedPermission>): Response

    private fun checkThesePermissionsAreInsufficient(
            permissions: Set<ReifiedPermission>,
            assertionText: String
    )
    {
        val response = requestWithPermissions(permissions)

        Assertions.assertThat(response.statusCode)
                .withFailMessage(assertionText)
                .isEqualTo(unsecuredResponseCode) // we return 404s for unauthorized users
    }

    private fun checkThesePermissionsAreSufficient(permissions: Set<ReifiedPermission>,
                                                   assertionText: String)
    {
        val response = requestWithPermissions(permissions)
        Assertions.assertThat(response.statusCode)
                .withFailMessage(assertionText)
                .isEqualTo(200)
    }

}