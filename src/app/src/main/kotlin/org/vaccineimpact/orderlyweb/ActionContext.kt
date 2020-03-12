package org.vaccineimpact.orderlyweb

import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import spark.Response

interface ActionContext
{
    val userProfile: CommonProfile?
    val permissions: PermissionSet
    val reportReadingScopes: List<String>

    fun contentType(): String

    fun queryString(): String?
    fun queryParams(key: String): String?
    fun params(): Map<String, String>
    fun params(key: String): String
    fun splat() : Array<String>?
    fun addResponseHeader(key: String, value: String)
    fun addDefaultResponseHeaders(contentType: String)

    fun hasPermission(requirement: ReifiedPermission): Boolean
    fun requirePermission(requirement: ReifiedPermission)

    fun isGlobalReader(): Boolean
    fun isReviewer(): Boolean

    fun getSparkResponse(): Response
    fun setStatusCode(statusCode: Int)
    fun<T> postData(): Map<String, T>

    @Throws(MissingParameterError::class)
    fun<T> postData(key: String): T
}