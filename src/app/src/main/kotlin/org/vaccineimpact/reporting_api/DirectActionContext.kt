package org.vaccineimpact.reporting_api

import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.sparkjava.SparkWebContext
import spark.Request
import spark.Response

open class DirectActionContext(private val context: SparkWebContext) : ActionContext {
    private val request
        get() = context.sparkRequest
    private val response
        get() = context.sparkResponse

    constructor(request: Request, response: Response)
            : this(SparkWebContext(request, response))

    override fun contentType(): String = request.contentType()
    override fun queryParams(key: String): String? = request.queryParams(key)
    override fun params(): Map<String, String> = request.params()
    override fun params(key: String): String = request.params(key)
    override fun addResponseHeader(key: String, value: String) {
        response.header(key, value)
    }

    override val userProfile: CommonProfile by lazy {
        val manager = ProfileManager<CommonProfile>(context)
        manager.getAll(false).single()
    }

    override fun getSparkResponse(): Response {
        return response
    }
}