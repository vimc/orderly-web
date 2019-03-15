package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.config.Config
import org.pac4j.core.config.ConfigFactory
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.db.TokenStore

class TokenVerifyingConfigFactory(
        private val requiredPermissions: Set<PermissionRequirement>
) : ConfigFactory
{
    companion object
    {
        val headerClientWrapper = CompressedJWTHeaderClientWrapper(WebTokenHelper.instance.verifier)
        val cookieClientWrapper = CompressedJWTCookieClientWrapper(WebTokenHelper.instance.verifier)
        val parameterClientWrapper = CompressedJWTParameterClientWrapper(
                WebTokenHelper.instance.verifier,
                TokenStore.instance
        )
        val githubDirectClientWrapper = GithubDirectClientWrapper()
    }

    val clientWrappers = mutableListOf(headerClientWrapper, cookieClientWrapper)

    override fun build(vararg parameters: Any?): Config
    {
        return Config(clientWrappers.map { it.client }).apply {
            setAuthorizer(MontaguAuthorizer(requiredPermissions))
            addMatcher(SkipOptionsMatcher.name, SkipOptionsMatcher)
            httpActionAdapter = OrderlyActionAdaptor(clientWrappers)
        }
    }

    fun allClients() = clientWrappers.joinToString { it.client::class.java.simpleName }

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
    this.clientWrappers.add(TokenVerifyingConfigFactory.parameterClientWrapper)
    return this
}

fun TokenVerifyingConfigFactory.githubAuthentication(): TokenVerifyingConfigFactory
{
    this.clientWrappers.clear()
    this.clientWrappers.add(TokenVerifyingConfigFactory.githubDirectClientWrapper)
    return this
}