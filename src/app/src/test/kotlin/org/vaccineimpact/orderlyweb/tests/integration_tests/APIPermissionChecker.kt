package org.vaccineimpact.orderlyweb.tests.integration_tests

import khttp.responses.Response
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.errors.InvalidOperationError
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.PermissionChecker
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.APIRequestHelper
import spark.route.HttpMethod

class APIPermissionChecker(url: String,
                           allRequiredPermissions: Set<ReifiedPermission>,
                           contentType: String = ContentTypes.html,
                           method: HttpMethod = HttpMethod.get,
                           postData: Map<String, String>? = null)
    : PermissionChecker(url, allRequiredPermissions, contentType, method, postData)
{

    override val unsecuredResponseCode = 403
    override val requestHelper = APIRequestHelper()
    override fun requestWithPermissions(permissions: Set<ReifiedPermission>): Response
    {
        return when (this.method)
        {
            HttpMethod.get ->
            {
                requestHelper.getWithPermissions(this.url, this.contentType, permissions)
            }
            HttpMethod.post ->
            {
                requestHelper.postWithPermissions(this.url, this.postData, this.contentType, permissions)
            }
            else ->
            {
                throw InvalidOperationError("Only GET and POST supported")
            }
        }
    }
}
