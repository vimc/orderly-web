package org.vaccineimpact.orderlyweb

import org.pac4j.core.config.ConfigFactory
import org.pac4j.sparkjava.SecurityFilter
import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.security.OrderlyWebSecurityLogic
import org.vaccineimpact.orderlyweb.security.SkipOptionsMatcher
import org.vaccineimpact.orderlyweb.security.WebSecurityConfigFactory
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationProvider
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebAuthenticationConfig
import org.vaccineimpact.orderlyweb.security.clients.OrderlyWebIndirectClient
import spark.route.HttpMethod
import kotlin.reflect.KClass

data class WebEndpoint(
        override val urlFragment: String,
        override val controller: KClass<*>,
        override val actionName: String,
        override val method: HttpMethod = HttpMethod.get,
        override val requiredPermissions: List<PermissionRequirement> = listOf(),
        override val secure: Boolean = false,
        override val transform: Boolean = false,
        override val contentType: String = ContentTypes.html,
        val externalAuth: Boolean = false,
        val spark: SparkWrapper = SparkServiceWrapper(),
        val configFactory: ConfigFactory? = null,
        val authenticationConfig: AuthenticationConfig = OrderlyWebAuthenticationConfig()
) : EndpointDefinition
{
    override val authenticateWithExternalProvider: Boolean = true

    override fun additionalSetup(url: String)
    {
        if (secure && authenticationConfig.useAuth)
        {
            addSecurityFilter(url)
        }

        if (this.contentType == ContentTypes.json)
        {
            spark.after(url, ContentTypes.json, DefaultHeadersFilter("${ContentTypes.json}; charset=utf-8"))
        }
        if (this.contentType == ContentTypes.text)
        {
            spark.after(url, ContentTypes.text, DefaultHeadersFilter("${ContentTypes.text}; charset=utf-8"))
        }
    }

    private fun addSecurityFilter(url: String)
    {
        // If Montagu Auth and if guest users are not allowed, OrderlyWeb auth should be fully synchronised
        // with the external login provider, and login page should not be seen
        val synchronisedAuth = authenticationConfig.getConfiguredProvider() == AuthenticationProvider.Montagu

        val client =
                if (externalAuth || synchronisedAuth)
                {
                    authenticationConfig.getAuthenticationIndirectClient()
                }
                else
                {
                    OrderlyWebIndirectClient()
                }

        val factory = configFactory ?: WebSecurityConfigFactory(
                client,
                this.requiredPermissions.toSet()
        )

        val config = factory.build()
        val filter = SecurityFilter(
                config,
                client.javaClass.simpleName,
                config.authorizers.map { it.key }.joinToString(","),
                SkipOptionsMatcher.name
        ).apply { securityLogic = OrderlyWebSecurityLogic() }
        spark.before(url, contentType, method, filter)
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

fun WebEndpoint.json(): WebEndpoint
{
    return this.copy(contentType = ContentTypes.json)
}

fun WebEndpoint.transform(): WebEndpoint
{
    return this.copy(transform = true)
}
