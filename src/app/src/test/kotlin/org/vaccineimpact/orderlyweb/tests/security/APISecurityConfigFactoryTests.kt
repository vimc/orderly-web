package org.vaccineimpact.orderlyweb.tests.security

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.authorization.authorizer.AbstractRequireElementAuthorizer
import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.security.APISecurityClientsConfigFactory
import org.vaccineimpact.orderlyweb.security.SkipOptionsMatcher
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyWebAPIAuthorizer
import org.vaccineimpact.orderlyweb.security.clients.*
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class APISecurityClientsConfigFactoryTests: TeamcityTests()
{
    @Test
    fun `gets all clients`()
    {
        val sut = APISecurityClientsConfigFactory()
        val result = sut.allClients().split(", ")

        assertThat(result.count()).isEqualTo(1)
        assertThat(result.first()).isEqualTo("JWTHeaderClient")
    }

    @Test
    fun `sets requiredPermissions`()
    {
        val sut = APISecurityClientsConfigFactory()
        val requiredPermissions = setOf(PermissionRequirement.parse("*/testperm"))

        val result = sut.setRequiredPermissions(requiredPermissions)

        assertThat(result).isEqualTo(sut)
        val field = APISecurityClientsConfigFactory::class.java.getDeclaredField("requiredPermissions")
        field.isAccessible = true
        assertThat(field.get(result)).isEqualTo(requiredPermissions)
    }

    @Test
    fun `allows parameter authentication`()
    {
        val sut = APISecurityClientsConfigFactory()
        val result = sut.allowParameterAuthentication()

        assertThat(result).isEqualTo(sut)
        val allClients = result.allClients().split(", ")
        assertThat(allClients.count()).isEqualTo(2)
        assertThat(allClients[0]).isEqualTo("JWTHeaderClient")
        assertThat(allClients[1]).isEqualTo("JWTParameterClient")
    }

    @Test
    fun `allows external authentication with Montagu if configured auth provider`()
    {
        val mockAuthConfig = mock<AuthenticationConfig> () {
            on { getAuthenticationDirectClient() } doReturn MontaguDirectClient()
        }
        val sut = APISecurityClientsConfigFactory(mockAuthConfig)
        val result = sut.externalAuthentication()

        assertThat(result).isEqualTo(sut)
        val allClients = result.allClients().split(", ")
        assertThat(allClients.count()).isEqualTo(1)
        assertThat(allClients[0]).isEqualTo("MontaguDirectClient")
    }

    @Test
    fun `allows external authentication with GitHub if configured auth provider`()
    {
        val mockAuthConfig = mock<AuthenticationConfig> () {
            on { getAuthenticationDirectClient() } doReturn GitHubDirectClient()
        }
        val sut = APISecurityClientsConfigFactory(mockAuthConfig)
        val result = sut.externalAuthentication()

        assertThat(result).isEqualTo(sut)
        val allClients = result.allClients().split(", ")
        assertThat(allClients.count()).isEqualTo(1)
        assertThat(allClients[0]).isEqualTo("GitHubDirectClient")
    }

    @Test
    fun `builds expected config`()
    {
        val sut = APISecurityClientsConfigFactory()
        sut.allowParameterAuthentication()
        val requiredPermissions = setOf(PermissionRequirement.parse("*/testperm"))
        sut.setRequiredPermissions(requiredPermissions)

        val result = sut.build()

        assertThat(result.clients.clients.count()).isEqualTo(2)
        assertThat(result.clients.clients[0] is JWTHeaderClient).isTrue()
        assertThat(result.clients.clients[1] is JWTParameterClient).isTrue()

        assertThat(result.matchers.count()).isEqualTo(1)
        assertThat(result.matchers.entries.first().key).isEqualTo("SkipOptions")
        assertThat(result.matchers.entries.first().value).isEqualTo(SkipOptionsMatcher)

        assertThat(result.httpActionAdapter is APIActionAdaptor).isTrue()

        assertThat(result.authorizers.count()).isEqualTo(1)
        assertThat(result.authorizers.entries.first().key).isEqualTo("OrderlyWebAPIAuthorizer")
        val authorizer = result.authorizers.entries.first().value
        assertThat(authorizer is OrderlyWebAPIAuthorizer).isTrue()
        val baseClass = AbstractRequireElementAuthorizer::class.java
        val field = baseClass.getDeclaredField("elements")
        field.isAccessible = true
        val authElements = field.get(authorizer)

        assertThat(authElements).isEqualTo(requiredPermissions)
    }

}