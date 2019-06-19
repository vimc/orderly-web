package org.vaccineimpact.orderlyweb.tests.integration_tests

import khttp.post
import org.assertj.core.api.Assertions
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.removePermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.WebRequestHelper
import spark.route.HttpMethod

class WebPermissionChecker(private val url: String,
                           private val allRequiredPermissions: Set<ReifiedPermission>,
                           private val contentType: String = ContentTypes.html,
                           private val method: HttpMethod = HttpMethod.get,
                           private val postData: Map<String, String>? = null)
{

    private val testUserEmail = "test.user@example.com"
    private val webRequestHelper = WebRequestHelper()

    fun checkPermissionsAreSufficient() {
        checkThesePermissionsAreSufficient(allRequiredPermissions,
                assertionTextFormat = "Expected status code 200 for $url with the given permissions but got %s")
    }

    fun checkPermissionIsRequired(
            permission: ReifiedPermission
    )
    {
        val assertionText = "Expected permission '$permission' to be required for $url"
        val limitedPermissions = allRequiredPermissions - permission
        removePermission(testUserEmail, permission.name, permission.scope.databaseScopePrefix?: "")

        println("Checking that permission '$permission' is required for $url")
        checkThesePermissionsAreInsufficient(limitedPermissions, assertionText)

        if (permission.scope is Scope.Specific)
        {
            val scope = permission.scope as Scope.Specific

            println("Checking that same permission with different scope will not satisfy the requirement")
            val badPermission = ReifiedPermission(permission.name, Scope.Specific(scope.databaseScopePrefix, "bad-id"))
            checkThesePermissionsAreInsufficient(limitedPermissions + badPermission, assertionText)

            println("Checking that same permission with the global scope WILL satisfy the requirement")
            val betterPermission = ReifiedPermission(permission.name, Scope.Global())
            checkThesePermissionsAreSufficient(limitedPermissions + betterPermission,
                    "Expected to be able to substitute '$betterPermission' in place of '$permission' for $url")
        }
    }

    private fun checkThesePermissionsAreInsufficient(
            permissions: Set<ReifiedPermission>,
            assertionText: String
    )
    {
        webRequestHelper.getWebPage("/logout")

        val response = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                permissions,
                contentType,
                method,
                postData)
        Assertions.assertThat(response.statusCode)
                .withFailMessage(assertionText)
                .isEqualTo(404) // we return 404s for unauthorized users

    }

    private fun checkThesePermissionsAreSufficient(permissions: Set<ReifiedPermission>,
                                                   assertionText: String? = null,
                                                   assertionTextFormat: String? = null)
    {
        webRequestHelper.getWebPage("/logout")

        val response = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                permissions, contentType, method, postData)
        Assertions.assertThat(response.statusCode)
                .withFailMessage(assertionTextFormat?.format(response.statusCode)?: assertionText)
                .isEqualTo(200)

    }
}