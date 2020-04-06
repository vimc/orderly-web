package org.vaccineimpact.orderlyweb

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.GsonBuilder
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.errors.MissingRequiredPermissionError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.security.authorization.orderlyWebPermissions
import org.vaccineimpact.orderlyweb.viewmodels.PermissionViewModel
import spark.Request
import spark.Response

open class DirectActionContext(private val context: SparkWebContext,
                               private val appConfig: Config = AppConfig(),
                               private val profileManager: ProfileManager<CommonProfile>? = null) : ActionContext
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
    override fun splat(): Array<String>? = request.splat()
    override fun addResponseHeader(key: String, value: String)
    {
        response.header(key, value)
    }

    override fun addDefaultResponseHeaders(contentType: String)
    {
        addDefaultResponseHeaders(response.raw(), contentType = contentType)
    }

    override val userProfile: CommonProfile? by lazy {
        val manager = profileManager ?: ProfileManager<CommonProfile>(context)
        manager.getAll(true).singleOrNull()
    }

    override val permissions by lazy {
        userProfile?.orderlyWebPermissions ?: PermissionSet()
    }

    override fun isGlobalReader() = hasPermission(ReifiedPermission("reports.read", Scope.Global()))
    override fun isReviewer() = hasPermission(ReifiedPermission("reports.review", Scope.Global()))

    override val reportReadingScopes by lazy {
        permissions
                .filter { it.name == "reports.read" && it.scope.databaseScopePrefix == "report" }
                .map { it.scope.databaseScopeId }
    }

    override fun hasPermission(requirement: ReifiedPermission): Boolean
    {
        return if (appConfig.authorizationEnabled)
        {
            permissions.any { requirement.satisfiedBy(it) }
        }
        else
        {
            true
        }
    }

    override fun requirePermission(requirement: ReifiedPermission)
    {
        if (!hasPermission(requirement))
        {
            throw MissingRequiredPermissionError(setOf(requirement))
        }
    }

    override fun getSparkResponse(): Response
    {
        return response
    }

    override fun setStatusCode(statusCode: Int)
    {
        response.status(statusCode)
    }

    override fun<T> postData(): Map<String, T>
    {
        val body = request.body()

        return if (body.isNullOrEmpty())
        {
            emptyMap()
        }
        else
        {
            GsonBuilder().create().fromJson(request.body())
        }
    }

    override fun<T> postData(key: String): T
    {
        return postData<T>()[key] ?: throw MissingParameterError(key);
    }

    override fun getRequestBody(): String
    {
        return request.body()
    }
}

fun ActionContext.permissionFromPostData(): ReifiedPermission
{
    val postData = this.postData<String>()
    val permission = PermissionViewModel(
            postData["name"] ?: throw MissingParameterError("name"),
            postData["scope_prefix"],
            postData["scope_id"],
            ""
    )

    return ReifiedPermission(permission.name,
            Scope.parse(permission))
}

fun ActionContext.permissionFromRouteParams(): ReifiedPermission {
    val name = this.params(":name")
    val scopePrefix = this.queryParams("scopePrefix")
    val scopeId = this.queryParams("scopeId")
    return ReifiedPermission(name, Scope.parse(scopePrefix, scopeId))
}