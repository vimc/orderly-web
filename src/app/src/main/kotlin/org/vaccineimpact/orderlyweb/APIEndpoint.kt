package org.vaccineimpact.orderlyweb

import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.security.APISecurityConfigFactory
import org.vaccineimpact.orderlyweb.security.SkipOptionsMatcher
import org.vaccineimpact.orderlyweb.security.allowParameterAuthentication
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyWebAuthorizer
import org.vaccineimpact.orderlyweb.security.externalAuthentication
import spark.Spark
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
        override val secure: Boolean = false

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
            Spark.after(url, ContentTypes.json, DefaultHeadersFilter("${ContentTypes.json}; charset=utf-8"))
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

        Spark.before(url, org.pac4j.sparkjava.SecurityFilter(
                config,
                configFactory.allClients(),
                OrderlyWebAuthorizer::class.java.simpleName,
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

fun APIEndpoint.githubAuth(): APIEndpoint
{
    return this.copy(authenticateWithExternalProvider = true)
}

fun APIEndpoint.post(): APIEndpoint
{
    return this.copy(method = HttpMethod.post)
}