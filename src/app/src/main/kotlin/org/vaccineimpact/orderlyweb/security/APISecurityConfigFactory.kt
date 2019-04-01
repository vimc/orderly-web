package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer
import org.pac4j.core.client.Client
import org.pac4j.core.config.Config
import org.pac4j.core.config.ConfigFactory
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.TokenStore
import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyWebAuthorizer
import org.vaccineimpact.orderlyweb.security.clients.*

class APISecurityConfigFactory(
        private val requiredPermissions: Set<PermissionRequirement>
) : ConfigFactory
{
    companion object
    {
        val headerClient = JWTHeaderClient(WebTokenHelper.instance.verifier)
        val parameterClient = JWTParameterClient(WebTokenHelper.instance.verifier, TokenStore.instance)
        val githubDirectClient = GithubDirectClient()
    }

    val allClients = mutableListOf<OrderlyWebTokenCredentialClient>(headerClient)

    override fun build(vararg parameters: Any?): Config
    {
        @Suppress("UNCHECKED_CAST")
        return Config(allClients as List<Client<Credentials, CommonProfile>>).apply {
            addMatcher(SkipOptionsMatcher.name, SkipOptionsMatcher)
            httpActionAdapter = APIActionAdaptor(allClients)

            if (AppConfig().authorizationEnabled)
            {
                setAuthorizer(OrderlyWebAuthorizer(requiredPermissions))
            }
            else
            {
                setAuthorizer(IsAuthenticatedAuthorizer<CommonProfile>())
            }
        }
    }

    fun allClients() = allClients.joinToString { it::class.java.simpleName }

}

fun APISecurityConfigFactory.allowParameterAuthentication(): APISecurityConfigFactory
{
    this.allClients.add(APISecurityConfigFactory.parameterClient)
    return this
}

fun APISecurityConfigFactory.externalAuthentication(): APISecurityConfigFactory
{
    this.allClients.clear()
    // TODO add Montagu direct client and use if configured to do so
    this.allClients.add(APISecurityConfigFactory.githubDirectClient)
    return this
}