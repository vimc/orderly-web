package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.config.Config
import org.pac4j.core.config.ConfigFactory
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyWebAuthorizer

class WebSecurityConfigFactory(
        private val client: IndirectClient,
        private val requiredPermissions: Set<PermissionRequirement>
) : ConfigFactory
{
    override fun build(vararg parameters: Any?): Config
    {
        return Config(client).apply {
            addMatcher(SkipOptionsMatcher.name, SkipOptionsMatcher)
            httpActionAdapter = WebActionAdaptor()

            if (AppConfig().authorizationEnabled)
            {
                setAuthorizer(OrderlyWebAuthorizer(requiredPermissions))
            }
            else
            {
                setAuthorizer(IsAuthenticatedAuthorizer())
            }
        }
    }
}
