package org.vaccineimpact.reporting_api

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonParser
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.reporting_api.security.montaguPermissions
import spark.Request
import spark.Response
import org.vaccineimpact.reporting_api.errors.MissingRequiredPermissionError

open class DirectActionContext(private val context: SparkWebContext) : ActionContext
{
    private val request
        get() = context.sparkRequest
    private val response
        get() = context.sparkResponse

    constructor(request: Request, response: Response)
            : this(SparkWebContext(request, response))

    override fun contentType(): String = request.contentType()
    override fun queryParams(key: String): String? = request.queryParams(key)
    override fun queryString(): String? = request.queryString()
    override fun params(): Map<String, String> = request.params()
    override fun params(key: String): String = request.params(key)
    override fun addResponseHeader(key: String, value: String)
    {
        response.header(key, value)
    }

    override fun addDefaultResponseHeaders(contentType: String)
    {
        addDefaultResponseHeaders(response.raw(), contentType = contentType)
    }

    override val userProfile: CommonProfile by lazy {
        val manager = ProfileManager<CommonProfile>(context)
        manager.getAll(false).single()
    }

    override val permissions by lazy {
        userProfile.montaguPermissions()
    }

    override fun hasPermission(requirement: ReifiedPermission)
            = permissions.any { requirement.satisfiedBy(it) }

    override fun getSparkResponse(): Response
    {
        return response
    }

    override fun setStatusCode(statusCode: Int)
    {
        response.status(statusCode)
    }
    override fun postData(): Map<String, String>
    {
        val body = request.body()

        if (body.isNullOrEmpty())
            return emptyMap()

        return GsonBuilder().create()
                .fromJson<Map<String, String>>(request.body())
    }
}