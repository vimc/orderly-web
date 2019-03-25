package org.vaccineimpact.orderlyweb

import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.security.*
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyWebAuthorizer
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
        override val allowParameterAuthentication: Boolean = false,
        override val authenticateWithGithub: Boolean = false,
        override val authenticateWithMontagu: Boolean = false,
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

        var configFactory = TokenVerifyingConfigFactory(
                this.requiredPermissions.toSet())

        if (allowParameterAuthentication)
        {
            configFactory = configFactory.allowParameterAuthentication()
        }

        if (authenticateWithGithub)
        {
            configFactory = configFactory.githubAuthentication()
        }

        if (authenticateWithMontagu)
        {
            configFactory = configFactory.montaguAuthentication()
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

fun Endpoint.allowParameterAuthentication(): Endpoint
{
    return this.copy(allowParameterAuthentication = true)
}

fun Endpoint.secure(permissions: Set<String> = setOf()): Endpoint
{
    val allPermissions = (permissions).map {
        PermissionRequirement.parse(it)
    }
    return this.copy(requiredPermissions = allPermissions, secure = true)
}

fun Endpoint.transform(): Endpoint
{
    return this.copy(transform = true)
}

fun Endpoint.json(): Endpoint
{
    return this.copy(contentType = ContentTypes.json)
}

fun Endpoint.html(): Endpoint
{
    return this.copy(contentType = ContentTypes.html)
}

fun Endpoint.githubAuth(): Endpoint
{
    return this.copy(authenticateWithGithub = true)
}

fun Endpoint.montaguAuth(): Endpoint
{
    return this.copy(authenticateWithMontagu = true)
}

fun Endpoint.post(): Endpoint
{
    return this.copy(method = HttpMethod.post)
}