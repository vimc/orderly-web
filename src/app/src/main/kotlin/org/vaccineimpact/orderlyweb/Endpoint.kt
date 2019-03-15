package org.vaccineimpact.orderlyweb

import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.security.MontaguAuthorizer
import org.vaccineimpact.orderlyweb.security.PermissionRequirement
import org.vaccineimpact.orderlyweb.security.TokenVerifyingConfigFactory
import org.vaccineimpact.orderlyweb.security.allowParameterAuthentication
import spark.Spark
import spark.route.HttpMethod
import kotlin.reflect.KClass

data class Endpoint(
        override val urlFragment: String,
        override val controller: KClass<*>,
        override val actionName: String,
        override val contentType: String = ContentTypes.binarydata,
        override val method: HttpMethod = HttpMethod.get,
        override val transform: Boolean = false,
        override val requiredPermissions: List<PermissionRequirement> = listOf(),
        override val allowParameterAuthentication: Boolean = false

) : EndpointDefinition
{
    init
    {
        if (!urlFragment.endsWith("/"))
        {
            throw Exception("All endpoint definitions must end with a forward slash: $urlFragment")
        }
    }

    override fun additionalSetup(url: String)
    {
        if (requiredPermissions.any())
        {
            addSecurityFilter(url)
        }
        if (this.contentType == ContentTypes.json)
        {
            Spark.after(url, ContentTypes.json, DefaultHeadersFilter("${ContentTypes.json}; charset=utf-8"))
        }
    }

    private fun addSecurityFilter(url: String)
    {
        if (AppConfig().authEnabled)
        {
            var configFactory = TokenVerifyingConfigFactory(
                    this.requiredPermissions.toSet())

            if (allowParameterAuthentication)
            {
                configFactory = configFactory.allowParameterAuthentication()
            }

            val config = configFactory.build()

            Spark.before(url, org.pac4j.sparkjava.SecurityFilter(
                    config,
                    configFactory.allClients(),
                    MontaguAuthorizer::class.java.simpleName,
                    "SkipOptions"
            ))
        }
    }

}

fun Endpoint.allowParameterAuthentication(): Endpoint
{
    return this.copy(allowParameterAuthentication = true)
}

fun Endpoint.secure(permissions: Set<String> = setOf()): Endpoint
{
    val allPermissions = (permissions + "*/can-login").map {
        PermissionRequirement.parse(it)
    }
    return this.copy(requiredPermissions = allPermissions)
}

fun Endpoint.transform(): Endpoint
{
    return this.copy(transform = true)
}

fun Endpoint.json(): Endpoint
{
    return this.copy(contentType = ContentTypes.json)
}