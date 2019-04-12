package org.vaccineimpact.orderlyweb

import org.pac4j.core.client.IndirectClient
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationProvider
import org.vaccineimpact.orderlyweb.security.SkipOptionsMatcher
import org.vaccineimpact.orderlyweb.security.WebSecurityConfigFactory
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyWebAuthorizer
import org.vaccineimpact.orderlyweb.security.clients.GithubIndirectClient
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClient
import org.vaccineimpact.orderlyweb.security.clients.OrderlyWebIndirectClient

import spark.Spark
import spark.route.HttpMethod
import kotlin.reflect.KClass

data class WebEndpoint(
        override val urlFragment: String,
        override val controller: KClass<*>,
        override val actionName: String,
        override val method: HttpMethod = HttpMethod.get,
        override val requiredPermissions: List<PermissionRequirement> = listOf(),
        override val secure: Boolean = false,
        val externalAuth: Boolean = false
) : EndpointDefinition
{
    override val transform = false
    override val contentType = ContentTypes.html
    override val allowParameterAuthentication = false
    override val authenticateWithExternalProvider: Boolean = true

    override fun additionalSetup(url: String)
    {
        if (secure)
        {
           addSecurityFilter(url)
        }
    }

    private fun addSecurityFilter(url: String)
    {
        //The endpoints with externalAuth are those which will redirect the user to a specific external Auth provider
        //after clicking a link on our login page. All others redirect to the login page
        val client =
            if (externalAuth)
            {
                AuthenticationConfig.getAuthenticationIndirectClient()
            }
            else
            {
                 OrderlyWebIndirectClient()
            }

        val configFactory = WebSecurityConfigFactory(
                client,
                this.requiredPermissions.toSet())

        val config = configFactory.build()

        Spark.before(url, org.pac4j.sparkjava.SecurityFilter(
                config,
                client.javaClass.simpleName,
                config.authorizers.map { it.key }.joinToString(","),
                SkipOptionsMatcher.name
        ))

    }

}

fun WebEndpoint.secure(permissions: Set<String> = setOf(), externalAuth: Boolean = false): WebEndpoint
{
    val allPermissions = (permissions).map {
        PermissionRequirement.parse(it)
    }
    return this.copy(requiredPermissions = allPermissions, secure = true, externalAuth = externalAuth)
}

fun WebEndpoint.post(): WebEndpoint
{
    return this.copy(method = HttpMethod.post)
}
