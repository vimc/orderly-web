package org.vaccineimpact.orderlyweb.security.authentication

import org.pac4j.core.client.IndirectClient
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.credentials.Credentials
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClient
import org.vaccineimpact.orderlyweb.security.clients.GithubIndirectClient

class AuthenticationConfig
{
    companion object {

        fun getConfiguredProvider(): AuthenticationProvider {
            var configuredValue = AppConfig()["auth.provider"]
            return when (configuredValue.toLowerCase()) {
                "github" -> AuthenticationProvider.Github
                "montagu" -> AuthenticationProvider.Montagu
            else -> throw UnknownAuthenticationProvider(configuredValue)
            }
        }

        fun getGithubOAuthKey(): String {
            // AKA the Client ID
            return AppConfig()["auth.github_key"]
        }

        fun getGithubOAuthSecret(): String {
            return AppConfig()["auth.github_secret"]
        }

        fun getAuthenticationIndirectClient() : IndirectClient<out Credentials, out CommonProfile>
        {
            return  when (getConfiguredProvider()) {
                AuthenticationProvider.Github -> GithubIndirectClient(getGithubOAuthKey(), getGithubOAuthSecret())
                AuthenticationProvider.Montagu -> MontaguIndirectClient()
            }
        }
    }
}

enum class AuthenticationProvider
{
    Montagu,
    Github
}

class UnknownAuthenticationProvider(val provider: String) : Exception("Application is configured to use unknown authentication provider '$provider'")
