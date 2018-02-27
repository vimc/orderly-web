package org.vaccineimpact.reporting_api.security

import org.pac4j.core.config.Config
import org.pac4j.core.config.ConfigFactory
import org.pac4j.core.profile.CommonProfile
import org.pac4j.jwt.profile.JwtProfile
import org.vaccineimpact.api.models.permissions.PermissionSet
import org.vaccineimpact.reporting_api.db.TokenStore

class TokenVerifyingConfigFactory(
        private val requiredPermissions: Set<PermissionRequirement>
) : ConfigFactory
{
    companion object
    {
        val headerClientWrapper = JWTHeaderClientWrapper(TokenVerifier(KeyHelper.authPublicKey,
                org.vaccineimpact.reporting_api.db.AppConfig()["token.issuer"]))

        val parameterClientWrapper = JWTParameterClientWrapper(
                WebTokenHelper.oneTimeTokenHelper.verifier,
                TokenStore.instance
        )
    }

    val clientWrappers = mutableListOf<MontaguCredentialClientWrapper>(headerClientWrapper)

    override fun build(vararg parameters: Any?): Config
    {
        clientWrappers.forEach {
            it.client.addAuthorizationGenerator({ _, profile -> extractPermissionsFromToken(profile) })
        }

        return Config(clientWrappers.map { it.client }).apply {
            setAuthorizer(MontaguAuthorizer(requiredPermissions))
            addMatcher(SkipOptionsMatcher.name, SkipOptionsMatcher)
            httpActionAdapter = TokenActionAdapter(clientWrappers)
        }
    }

    fun allClients() = clientWrappers.map { it.client::class.java.simpleName }.joinToString()

    private fun extractPermissionsFromToken(profile: CommonProfile): CommonProfile
    {
        // "permissions" will exists as an attribute because profile is a JwtProfile
        val permissions = PermissionSet((profile.getAttribute("permissions") as String)
                .split(',')
                .filter { it.isNotEmpty() }
        )
        profile.montaguPermissions = permissions
        return profile
    }

}

fun TokenVerifyingConfigFactory.allowParameterAuthentication(): TokenVerifyingConfigFactory
{
    this.clientWrappers.add(TokenVerifyingConfigFactory.parameterClientWrapper)
    return this
}