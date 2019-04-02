package org.vaccineimpact.orderlyweb.security.authentication

import org.vaccineimpact.orderlyweb.db.AppConfig

enum class AuthenticationProvider {
    Montagu,
    Github;

    companion object {
        fun getConfiguredProvider(): AuthenticationProvider {
            var configuredValue = AppConfig()["app.authentication_provider"]
            return when (configuredValue.toLowerCase()) {
                "github" -> AuthenticationProvider.Github
                "montagu" -> AuthenticationProvider.Montagu
            else -> throw UnknownAuthenticationProvider(configuredValue)
            }

        }


        fun getGithubOAuthKey(): String {
            return ""
        }

        fun getGithubOAuthSecret(): String {
            //TODO: Making this hardcoded to the test account client
            return ""
        }
    }
}

class UnknownAuthenticationProvider(val provider: String) : Exception("Application is configured to use unknown authentication provider '$provider'")
