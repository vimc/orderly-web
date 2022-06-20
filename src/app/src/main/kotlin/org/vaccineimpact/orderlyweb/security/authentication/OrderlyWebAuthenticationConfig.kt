package org.vaccineimpact.orderlyweb.security.authentication

import org.pac4j.core.client.IndirectClient
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.profile.CommonProfile
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.repositories.OrderlySettingsRepository
import org.vaccineimpact.orderlyweb.db.repositories.SettingsRepository
import org.vaccineimpact.orderlyweb.security.clients.*

interface AuthenticationConfig
{
    val allowGuestUser: Boolean
    val canAllowGuestUser: Boolean
    fun getConfiguredProvider(): AuthenticationProvider
    fun getAuthenticationIndirectClient(): IndirectClient<out Credentials, out CommonProfile>
    fun getAuthenticationDirectClient(): OrderlyWebTokenCredentialClient
}

class OrderlyWebAuthenticationConfig(val appConfig: Config = AppConfig(),
                                     val settingsRepo: SettingsRepository = OrderlySettingsRepository()) : AuthenticationConfig
{
    override val allowGuestUser: Boolean
        get()
        {
            return if (canAllowGuestUser)
            {
                settingsRepo.getAuthAllowGuest()
            }
            else
            {
                false
            }
        }

    override val canAllowGuestUser: Boolean
        get()
        {
            return appConfig.authorizationEnabled && (getConfiguredProvider() != AuthenticationProvider.Montagu)
        }

    override fun getConfiguredProvider(): AuthenticationProvider
    {
        val configuredValue = appConfig["auth.provider"]

        return when (configuredValue.lowercase())
        {
            "github" -> AuthenticationProvider.GitHub
            "montagu" -> AuthenticationProvider.Montagu
            else -> throw UnknownAuthenticationProvider(configuredValue)
        }
    }

    private fun getGithubOAuthKey(): String
    {
        // AKA the Client ID
        return appConfig["auth.github_key"]
    }

    private fun getGithubOAuthSecret(): String
    {
        return appConfig["auth.github_secret"]
    }

    override fun getAuthenticationIndirectClient(): IndirectClient<out Credentials, out CommonProfile>
    {
        return when (getConfiguredProvider())
        {
            AuthenticationProvider.GitHub -> GithubIndirectClient(getGithubOAuthKey(), getGithubOAuthSecret())
            AuthenticationProvider.Montagu -> MontaguIndirectClient()
        }
    }

    override fun getAuthenticationDirectClient(): OrderlyWebTokenCredentialClient
    {
        return when (getConfiguredProvider())
        {
            AuthenticationProvider.GitHub -> GitHubDirectClient()
            AuthenticationProvider.Montagu -> MontaguDirectClient()
        }
    }

}

enum class AuthenticationProvider
{
    Montagu,
    GitHub
}

class UnknownAuthenticationProvider(val provider: String) : Exception("Application is configured to use unknown authentication provider '$provider'")
