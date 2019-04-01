package org.vaccineimpact.orderlyweb

import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.security.SkipOptionsMatcher
import org.vaccineimpact.orderlyweb.security.WebSecurityConfigFactory
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyWebAuthorizer
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClient
import spark.Spark
import spark.route.HttpMethod
import kotlin.reflect.KClass

data class WebEndpoint(
        override val urlFragment: String,
        override val controller: KClass<*>,
        override val actionName: String,
        override val method: HttpMethod = HttpMethod.get,
        override val requiredPermissions: List<PermissionRequirement> = listOf(),
        override val secure: Boolean = false
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
        // TODO use GitHub OAuth Client if configured to do so
        val configFactory = WebSecurityConfigFactory(
                MontaguIndirectClient(),
                this.requiredPermissions.toSet())

        val config = configFactory.build()

        Spark.before(url, org.pac4j.sparkjava.SecurityFilter(
                config,
                MontaguIndirectClient::class.java.simpleName,
                OrderlyWebAuthorizer::class.java.simpleName,
                SkipOptionsMatcher.name
        ))
    }

}

fun WebEndpoint.secure(permissions: Set<String> = setOf()): WebEndpoint
{
    val allPermissions = (permissions).map {
        PermissionRequirement.parse(it)
    }
    return this.copy(requiredPermissions = allPermissions, secure = true)
}

fun WebEndpoint.post(): WebEndpoint
{
    return this.copy(method = HttpMethod.post)
}
