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
                "none" -> AuthenticationProvider.None // For testing purposes, or may be instances which don't require any auth
            else -> throw UnknownAuthenticationProvider(configuredValue)
            }

        }

        fun getGithubOAuthKey(): String {
            // AKA the Client ID
            return AppConfig()["auth.github_key"]
        }

        fun getGithubOAuthSecret(): String {
            //TODO: Making this hardcoded to the test Github OAuth account client for now, but this should be taken from the vault,
            //not from config checked into out repo. However, it would be fine for the value to come from config
            //which is injected during deployment (we assume that the config on the server is secure), we just need
            //to make sure we don't store our production Montagu Github OAuth app secret in the repo!
            return "ef8550f711b65e0ab1457fe8470b2df03197b892"
        }

        fun getAuthenticationIndirectClient() : IndirectClient<out Credentials, out CommonProfile>
        {
            val result =  when (getConfiguredProvider()) {
                AuthenticationProvider.Github ->
                    {
                        val ghc = GithubIndirectClient(getGithubOAuthKey(), getGithubOAuthSecret())
                        return ghc

                    }
                else -> MontaguIndirectClient()
            }



            return result
        }
    }
}

enum class AuthenticationProvider
{
    Montagu,
    Github,
    None
}

class UnknownAuthenticationProvider(val provider: String) : Exception("Application is configured to use unknown authentication provider '$provider'")
