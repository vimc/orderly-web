package org.vaccineimpact.orderlyweb.tests.unit_tests.security

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.repositories.SettingsRepository
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebAuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationProvider
import org.vaccineimpact.orderlyweb.security.authentication.UnknownAuthenticationProvider
import org.vaccineimpact.orderlyweb.security.clients.GitHubDirectClient
import org.vaccineimpact.orderlyweb.security.clients.GithubIndirectClient
import org.vaccineimpact.orderlyweb.security.clients.MontaguDirectClient
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClient

class AuthenticationConfigTests
{
    @Test
    fun `getConfiguredProvider is case insensitive`()
    {
        var fakeConfig = mock<Config>() {
            on { get("auth.provider") } doReturn "Montagu"
        }
        var sut = OrderlyWebAuthenticationConfig(fakeConfig)
        var result = sut.getConfiguredProvider()

        assertThat(result).isEqualTo(AuthenticationProvider.Montagu)

        fakeConfig = mock {
            on { get("auth.provider") } doReturn "montagu"
        }
        sut = OrderlyWebAuthenticationConfig(fakeConfig)
        result = sut.getConfiguredProvider()

        assertThat(result).isEqualTo(AuthenticationProvider.Montagu)

        fakeConfig = mock {
            on { get("auth.provider") } doReturn "github"
        }
        sut = OrderlyWebAuthenticationConfig(fakeConfig)
        result = sut.getConfiguredProvider()

        assertThat(result).isEqualTo(AuthenticationProvider.GitHub)
    }

    @Test
    fun `getConfiguredProvider throws error if provider is not GitHub or Montagu`()
    {
        val fakeConfig = mock<Config> {
            on { get("auth.provider") } doReturn "nonsense"
        }
        val sut = OrderlyWebAuthenticationConfig(fakeConfig)

        assertThatThrownBy { sut.getConfiguredProvider() }
                .isInstanceOf(UnknownAuthenticationProvider::class.java)
    }

    @Test
    fun `getAuthenticationIndirectClient returns Montagu client if provider is Montagu`()
    {
        val fakeConfig = mock<Config> {
            on { get("auth.provider") } doReturn "montagu"
        }
        val sut = OrderlyWebAuthenticationConfig(fakeConfig)
        val result = sut.getAuthenticationIndirectClient()

        assertThat(result is MontaguIndirectClient).isTrue()
    }

    @Test
    fun `getAuthenticationIndirectClient returns GitHub client if provider is GitHub`()
    {
        val fakeConfig = mock<Config> {
            on { get("auth.provider") } doReturn "github"
            on { get("auth.github_key") } doReturn "key"
            on { get("auth.github_secret") } doReturn "secret"
        }
        val sut = OrderlyWebAuthenticationConfig(fakeConfig)
        val result = sut.getAuthenticationIndirectClient()

        assertThat(result is GithubIndirectClient).isTrue()
    }

    @Test
    fun `getAuthenticationDirectClient returns Montagu client if provider is Montagu`()
    {
        val fakeConfig = mock<Config> {
            on { get("auth.provider") } doReturn "montagu"
        }
        val sut = OrderlyWebAuthenticationConfig(fakeConfig)
        val result = sut.getAuthenticationDirectClient()

        assertThat(result is MontaguDirectClient).isTrue()
    }

    @Test
    fun `getAuthenticationDirectClient returns GitHub client if provider is GitHub`()
    {
        val fakeConfig = mock<Config> {
            on { get("auth.provider") } doReturn "github"
            on { get("auth.github_key") } doReturn "key"
            on { get("auth.github_secret") } doReturn "secret"
        }
        val sut = OrderlyWebAuthenticationConfig(fakeConfig)
        val result = sut.getAuthenticationDirectClient()

        assertThat(result is GitHubDirectClient).isTrue()
    }

    @Test
    fun `allowGuestUser returns expected result`()
    {
        val mockSettingsRepo = mock<SettingsRepository>{
            on { getAuthAllowGuest() } doReturn true
        }
        val mockConfig = mock<Config>{
            on { authorizationEnabled } doReturn true
            on { get("auth.provider") } doReturn AuthenticationProvider.GitHub.toString()
        }
        val sut = OrderlyWebAuthenticationConfig(mockConfig, mockSettingsRepo)
        val result = sut.allowGuestUser

        assertThat(result).isTrue()
    }

    @Test
    fun `allowGuestUser ignores repo content and returns false if cannot allow guest`()
    {
        val mockSettingsRepo = mock<SettingsRepository>{
            on { getAuthAllowGuest() } doReturn true
        }
        val mockConfig = mock<Config>{
            on { authorizationEnabled } doReturn false
        }
        val sut = OrderlyWebAuthenticationConfig(mockConfig, mockSettingsRepo)
        val result = sut.allowGuestUser

        assertThat(result).isFalse()
    }

    @Test
    fun `canAllowGuestUser returns false if auth provider is Montagu`()
    {
        val mockConfig = mock<Config>{
            on { authorizationEnabled } doReturn true
            on { get("auth.provider") } doReturn AuthenticationProvider.Montagu.toString()
        }
        val sut = OrderlyWebAuthenticationConfig(mockConfig, mock())
        assertThat(sut.canAllowGuestUser).isFalse()
    }

    @Test
    fun `canAllowGuestUser returns false if authorization is not enabled`()
    {
        val mockConfig = mock<Config>{
            on { authorizationEnabled } doReturn false
            on { get("auth.provider") } doReturn AuthenticationProvider.GitHub.toString()
        }
        val sut = OrderlyWebAuthenticationConfig(mockConfig, mock())
        assertThat(sut.canAllowGuestUser).isFalse()
    }

    @Test
    fun `canAllowGuestUser returns true if config allows guest user`()
    {
        val mockConfig = mock<Config>{
            on { authorizationEnabled } doReturn true
            on { get("auth.provider") } doReturn AuthenticationProvider.GitHub.toString()
        }
        val sut = OrderlyWebAuthenticationConfig(mockConfig, mock())
        assertThat(sut.canAllowGuestUser).isTrue()
    }
}
