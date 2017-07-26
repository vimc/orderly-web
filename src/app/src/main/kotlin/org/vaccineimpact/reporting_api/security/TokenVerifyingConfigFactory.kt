package org.vaccineimpact.reporting_api.security

import org.pac4j.core.config.Config
import org.pac4j.core.config.ConfigFactory
import org.pac4j.core.context.HttpConstants
import org.pac4j.core.profile.CommonProfile
import org.pac4j.jwt.profile.JwtProfile
import org.pac4j.sparkjava.DefaultHttpActionAdapter
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.api.models.Result
import org.vaccineimpact.api.models.ResultStatus
import org.vaccineimpact.api.models.permissions.PermissionSet
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.DirectActionContext
import org.vaccineimpact.reporting_api.Serializer
import org.vaccineimpact.reporting_api.app_start.addDefaultResponseHeaders
import org.vaccineimpact.reporting_api.errors.MissingRequiredPermissionError

class TokenVerifyingConfigFactory(
        private val clientWrappers: List<MontaguCredentialClientWrapper>,
        val requiredPermissions: Set<PermissionRequirement>
) : ConfigFactory
{
    override fun build(vararg parameters: Any?): Config
    {
        clientWrappers.forEach {
            it.client.addAuthorizationGenerator({ _, profile -> extractPermissionsFromToken(profile) })
        }

        return Config(clientWrappers.map{ it.client }).apply {
            setAuthorizer(MontaguAuthorizer(requiredPermissions))
            addMatcher(SkipOptionsMatcher.name, SkipOptionsMatcher)
            httpActionAdapter = TokenActionAdapter(clientWrappers)
        }
    }

    fun allClients() = clientWrappers.map { it.client::class.java.simpleName }.joinToString()

    private fun extractPermissionsFromToken(commonProfile: CommonProfile): CommonProfile
    {
        val profile = commonProfile as JwtProfile
        val permissions = PermissionSet((profile.getAttribute("permissions") as String)
                .split(',')
                .filter { it.isNotEmpty() }
        )
        commonProfile.addAttribute(PERMISSIONS, permissions)
        return commonProfile
    }
}

class TokenActionAdapter(clients: List<MontaguCredentialClientWrapper>) : DefaultHttpActionAdapter()
{
    val unauthorizedResponse: String = Serializer.instance.toJson(Result(
            ResultStatus.FAILURE,
            null,
            clients.map {
               it.errorInfo
           }
    ))

    fun forbiddenResponse(missingPermissions: Set<String>): String = Serializer.instance.toJson(Result(
            ResultStatus.FAILURE,
            null,
            MissingRequiredPermissionError(missingPermissions).problems
    ))

    override fun adapt(code: Int, context: SparkWebContext): Any? = when (code)
    {
        HttpConstants.UNAUTHORIZED ->
        {
            addDefaultResponseHeaders(context.response, ContentTypes.json)
            spark.Spark.halt(code, unauthorizedResponse)
        }
        HttpConstants.FORBIDDEN ->
        {
            addDefaultResponseHeaders(context.response, ContentTypes.json)
            val profile = DirectActionContext(context).userProfile
            val missingPermissions = profile.getAttributeOrDefault(MISSING_PERMISSIONS, mutableSetOf<String>())
            spark.Spark.halt(code, forbiddenResponse(missingPermissions))
        }
        else -> super.adapt(code, context)
    }
}