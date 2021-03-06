package org.vaccineimpact.orderlyweb.tests.integration_tests

import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.errors.InvalidOperationError
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.http.Response
import org.vaccineimpact.orderlyweb.tests.PermissionChecker
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.APIRequestHelper
import spark.route.HttpMethod

class APIPermissionChecker(url: String,
                           allRequiredPermissions: Set<ReifiedPermission>,
                           contentType: String = ContentTypes.html,
                           method: HttpMethod = HttpMethod.get,
                           postData: Map<String, Any>? = null)
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
            HttpMethod.delete ->
            {
                requestHelper.deleteWithPermissions(this.url, this.contentType, permissions)
            }
            else ->
            {
                throw InvalidOperationError("Only GET, POST and DELETE supported")
            }
        }
    }
}
