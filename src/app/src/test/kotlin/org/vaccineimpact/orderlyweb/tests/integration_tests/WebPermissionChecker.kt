package org.vaccineimpact.orderlyweb.tests.integration_tests

import khttp.responses.Response
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.PermissionChecker
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.WebRequestHelper
import spark.route.HttpMethod

class WebPermissionChecker(url: String,
                           allRequiredPermissions: Set<ReifiedPermission>,
                           contentType: String = ContentTypes.html,
                           method: HttpMethod = HttpMethod.get,
                           postData: Map<String, String>? = null)
    : PermissionChecker(url, allRequiredPermissions, contentType, method, postData)
{
    override val unsecuredResponseCode = 404
    override val requestHelper = WebRequestHelper()

    override fun requestWithPermissions(permissions: Set<ReifiedPermission>): Response
    {
        requestHelper.getWebPage("/logout")
        return requestHelper.loginWithMontaguAndMakeRequest(
                this.url,
                permissions,
                this.contentType,
                this.method,
                this.postData)
    }
}