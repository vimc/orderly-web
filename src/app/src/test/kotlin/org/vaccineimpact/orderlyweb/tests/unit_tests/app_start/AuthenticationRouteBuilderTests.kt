package org.vaccineimpact.orderlyweb.tests.unit_tests.app_start

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.vaccineimpact.orderlyweb.app_start.OrderlyAuthenticationRouteBuilder
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebAuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationProvider
import org.vaccineimpact.orderlyweb.security.clients.GithubIndirectClient
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClient
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class AuthenticationRouteBuilderTests : TeamcityTests()
{
    @Test
    fun `creates GitHub logout callback`()
    {
        val mockAuthConfig = mock<OrderlyWebAuthenticationConfig>() {
            on { getConfiguredProvider() } doReturn AuthenticationProvider.GitHub
            on { getAuthenticationIndirectClient() } doReturn GithubIndirectClient("fakekey", "fakesecret")
        }
        val sut = OrderlyAuthenticationRouteBuilder(mockAuthConfig)
        val result = sut.logout()
        assertThat(result.destroySession).isTrue()
        assertThat(result.centralLogout)
                .withFailMessage("Expected central logout to be false when GitHub is the configured provider")
                .isFalse()
        assertThat(result.defaultUrl).isEqualTo("/")
    }

    @Test
    fun `creates Montagu logout callback`()
    {
        val mockAuthConfig = mock<OrderlyWebAuthenticationConfig>() {
            on { getConfiguredProvider() } doReturn AuthenticationProvider.Montagu
            on { getAuthenticationIndirectClient() } doReturn MontaguIndirectClient()
        }
        val sut = OrderlyAuthenticationRouteBuilder(mockAuthConfig)
        val result = sut.logout()
        assertThat(result.destroySession).isTrue()
        assertThat(result.centralLogout)
                .withFailMessage("Expected central logout to be true when Montagu is the configured provider")
                .isTrue()
        assertThat(result.defaultUrl).isEqualTo("/")
    }
}
