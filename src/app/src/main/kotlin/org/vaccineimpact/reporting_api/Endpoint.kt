package org.vaccineimpact.reporting_api

import org.vaccineimpact.reporting_api.db.AppConfig
import org.vaccineimpact.reporting_api.security.MontaguAuthorizer
import org.vaccineimpact.reporting_api.security.PermissionRequirement
import org.vaccineimpact.reporting_api.security.TokenVerifyingConfigFactory
import org.vaccineimpact.reporting_api.security.allowParameterAuthentication
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
        override val requiredPermissions: Set<String> = emptySet(),
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
            val allPermissions = this.requiredPermissions.map {
                PermissionRequirement.parse(it)
            }

            var configFactory = TokenVerifyingConfigFactory(
                    allPermissions.toSet())

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

fun Endpoint.secure(permissions: Set<String>): Endpoint
{
    return this.copy(requiredPermissions = permissions)
}

fun Endpoint.transform(): Endpoint
{
    return this.copy(transform = true)
}

fun Endpoint.json(): Endpoint
{
    return this.copy(contentType = ContentTypes.json)
}