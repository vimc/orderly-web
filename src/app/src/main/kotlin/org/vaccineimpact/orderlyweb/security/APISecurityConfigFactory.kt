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
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyWebAPIAuthorizer
import org.vaccineimpact.orderlyweb.security.clients.*

interface APISecurityConfigFactory : ConfigFactory
{
    fun allClients(): String
    fun setRequiredPermissions(requiredPermissions: Set<PermissionRequirement>): APISecurityConfigFactory
    fun allowParameterAuthentication(): APISecurityConfigFactory
    fun externalAuthentication(): APISecurityConfigFactory
}


class APISecurityClientsConfigFactory(val authenticationConfig: AuthenticationConfig =
                                              AuthenticationConfig()) : APISecurityConfigFactory
{
    companion object
    {
        val headerClient = JWTHeaderClient(WebTokenHelper.instance.verifier)
        val parameterClient = JWTParameterClient(WebTokenHelper.instance.verifier, TokenStore.instance)
    }

    private val allClients = mutableListOf<OrderlyWebTokenCredentialClient>(headerClient)

    private var requiredPermissions: Set<PermissionRequirement>? = null

    override fun build(vararg parameters: Any?): Config
    {
        @Suppress("UNCHECKED_CAST")
        return Config(allClients as List<Client<Credentials, CommonProfile>>).apply {
            addMatcher(SkipOptionsMatcher.name, SkipOptionsMatcher)
            httpActionAdapter = APIActionAdaptor(allClients)

            if (AppConfig().authorizationEnabled)
            {
                setAuthorizer(OrderlyWebAPIAuthorizer(requiredPermissions!!))
            }
            else
            {
                setAuthorizer(IsAuthenticatedAuthorizer<CommonProfile>())
            }
        }
    }

    override fun allClients(): String
    {
        return allClients.joinToString { it::class.java.simpleName }
    }

    override fun setRequiredPermissions(requiredPermissions: Set<PermissionRequirement>): APISecurityConfigFactory
    {
        this.requiredPermissions = requiredPermissions
        return this
    }

    override fun allowParameterAuthentication(): APISecurityConfigFactory
    {
        allClients.add(APISecurityClientsConfigFactory.parameterClient)
        return this
    }

    override fun externalAuthentication(): APISecurityConfigFactory
    {
        allClients.clear()
        allClients.add(authenticationConfig.getAuthenticationDirectClient())
        return this
    }

}

