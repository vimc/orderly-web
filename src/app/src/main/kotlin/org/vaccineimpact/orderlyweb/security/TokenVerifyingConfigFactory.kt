package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.client.Client
import org.pac4j.core.client.DirectClient
import org.pac4j.core.config.Config
import org.pac4j.core.config.ConfigFactory
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.db.TokenStore
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet

class TokenVerifyingConfigFactory(
        private val requiredPermissions: Set<PermissionRequirement>
) : ConfigFactory
{
    companion object
    {
        val headerClient = JWTHeaderClient(WebTokenHelper.instance.verifier)
        val cookieClient = JWTCookieClient(WebTokenHelper.instance.verifier)
        val parameterClient = JWTParameterClient(WebTokenHelper.instance.verifier, TokenStore.instance)

    }

    val clients = mutableListOf<DirectClient<TokenCredentials, CommonProfile>>(headerClient, cookieClient)

    override fun build(vararg parameters: Any?): Config
    {
        @Suppress("UNCHECKED_CAST")
        return Config(clients as List<Client<Credentials, CommonProfile>>).apply {
            setAuthorizer(OrderlyWebAuthorizer(requiredPermissions))
            addMatcher(SkipOptionsMatcher.name, SkipOptionsMatcher)
            httpActionAdapter = TokenActionAdapter(listOf(JWTCookieClient(WebTokenHelper.instance.verifier)))
        }
    }

    fun allClients() = clients.joinToString { it::class.java.simpleName }

}

fun extractPermissionsFromToken(profile: CommonProfile): CommonProfile
{
    // "permissions" will exists as an attribute because profile is a JwtProfile
    val permissions = PermissionSet((profile.getAttribute("permissions") as String)
            .split(',')
            .filter { it.isNotEmpty() }
    )
    profile.montaguPermissions = permissions
    return profile
}

fun TokenVerifyingConfigFactory.allowParameterAuthentication(): TokenVerifyingConfigFactory
{
    this.clients.add(TokenVerifyingConfigFactory.parameterClient)
    return this
}