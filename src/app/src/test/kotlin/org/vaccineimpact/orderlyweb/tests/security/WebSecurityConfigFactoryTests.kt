package org.vaccineimpact.orderlyweb.tests.security

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.authorization.authorizer.AbstractRequireElementAuthorizer
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.security.SkipOptionsMatcher
import org.vaccineimpact.orderlyweb.security.WebActionAdaptor
import org.vaccineimpact.orderlyweb.security.WebSecurityConfigFactory
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyWebAuthorizer
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class WebSecurityConfigFactoryTests: TeamcityTests()
{
    @Test
    fun `builds expected config`()
    {
        val mockClient = mock<IndirectClient<Credentials, CommonProfile>>()

        val requiredPermission = PermissionRequirement.parse("*/testperm")

        val sut = WebSecurityConfigFactory(mockClient, setOf(requiredPermission))

        val result = sut.build()

        assertThat(result.clients.clients.count()).isEqualTo(1)
        assertThat(result.clients.clients.first()).isEqualTo(mockClient)

        assertThat(result.matchers.count()).isEqualTo(1)
        assertThat(result.matchers.entries.first().key).isEqualTo("SkipOptions")
        assertThat(result.matchers.entries.first().value).isEqualTo(SkipOptionsMatcher)

        assertThat(result.httpActionAdapter is WebActionAdaptor).isTrue()

        assertThat(result.authorizers.count()).isEqualTo(1)
        assertThat(result.authorizers.entries.first().key).isEqualTo("OrderlyWebAuthorizer")

        val authorizer = result.authorizers.entries.first().value
        assertThat(authorizer is OrderlyWebAuthorizer).isTrue()
        val baseClass = AbstractRequireElementAuthorizer::class.java
        val field = baseClass.getDeclaredField("elements")
        field.isAccessible = true
        val authElements = field.get(authorizer)

        assertThat(authElements is Set<*>).isTrue()
        assertThat((authElements as Set<*>).count()).isEqualTo(1)
        assertThat(authElements.first()).isEqualTo(requiredPermission)
    }
}