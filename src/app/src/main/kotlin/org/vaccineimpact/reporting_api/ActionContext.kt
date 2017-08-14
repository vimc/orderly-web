package org.vaccineimpact.reporting_api

import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.api.models.permissions.PermissionSet
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import spark.Response

interface ActionContext
{
    val userProfile: CommonProfile
    val permissions: PermissionSet

    fun contentType(): String
    fun queryParams(key: String): String?
    fun params(): Map<String, String>
    fun params(key: String): String
    fun addResponseHeader(key: String, value: String): Unit
    fun addDefaultResponseHeaders(contentType: String)

    fun hasPermission(requirement: ReifiedPermission): Boolean
    fun requirePermission(requirement: ReifiedPermission): Unit

    fun getSparkResponse(): Response
}