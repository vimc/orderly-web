package org.vaccineimpact.orderlyweb

import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.security.APISecurityConfigFactory
import org.vaccineimpact.orderlyweb.security.SkipOptionsMatcher
import org.vaccineimpact.orderlyweb.security.allowParameterAuthentication
import org.vaccineimpact.orderlyweb.security.externalAuthentication
import spark.route.HttpMethod
import kotlin.reflect.KClass

data class APIEndpoint(
        override val urlFragment: String,
        override val controller: KClass<*>,
        override val actionName: String,
        override val contentType: String = ContentTypes.binarydata,
        override val method: HttpMethod = HttpMethod.get,
        override val transform: Boolean = false,
        override val requiredPermissions: List<PermissionRequirement> = listOf(),
        override val authenticateWithExternalProvider: Boolean = false,
        override val allowParameterAuthentication: Boolean = false,
        override val secure: Boolean = false,
        val spark: SparkWrapper = SparkServiceWrapper()

) : EndpointDefinition
{
    override fun additionalSetup(url: String)
    {
        if (secure)
        {
            addSecurityFilter(url)
        }
        if (this.contentType == ContentTypes.json)
        {
            spark.after(url, ContentTypes.json, DefaultHeadersFilter("${ContentTypes.json}; charset=utf-8"))
        }
    }

    private fun addSecurityFilter(url: String)
    {
        var configFactory = APISecurityConfigFactory(
                this.requiredPermissions.toSet())

        if (allowParameterAuthentication)
        {
            configFactory = configFactory.allowParameterAuthentication()
        }

        if (authenticateWithExternalProvider)
        {
            configFactory = configFactory.externalAuthentication()
        }

        val config = configFactory.build()

        spark.before(url, org.pac4j.sparkjava.SecurityFilter(
                config,
                configFactory.allClients(),
                config.authorizers.map { it.key }.joinToString(","),
                SkipOptionsMatcher.name
        ))
    }

}

fun APIEndpoint.allowParameterAuthentication(): APIEndpoint
{
    return this.copy(allowParameterAuthentication = true)
}

fun APIEndpoint.secure(permissions: Set<String> = setOf()): APIEndpoint
{
    val allPermissions = (permissions).map {
        PermissionRequirement.parse(it)
    }
    return this.copy(requiredPermissions = allPermissions, secure = true)
}

fun APIEndpoint.transform(): APIEndpoint
{
    return this.copy(transform = true)
}

fun APIEndpoint.json(): APIEndpoint
{
    return this.copy(contentType = ContentTypes.json)
}

fun APIEndpoint.html(): APIEndpoint
{
    return this.copy(contentType = ContentTypes.html)
}

fun APIEndpoint.externalAuth(): APIEndpoint
{
    return this.copy(authenticateWithExternalProvider = true)
}

fun APIEndpoint.post(): APIEndpoint
{
    return this.copy(method = HttpMethod.post)
}