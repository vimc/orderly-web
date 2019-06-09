package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationProvider
import org.vaccineimpact.orderlyweb.security.authentication.UnknownAuthenticationProvider
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class AuthenticationConfigTests : TeamcityTests()
{
    @Test
    fun `getConfiguredProvider is case insensitive`()
    {
        var fakeConfig = mock<Config>() {
            on { get("auth.provider") } doReturn "Montagu"
        }
        var sut = AuthenticationConfig(fakeConfig)
        var result = sut.getConfiguredProvider()

        assertThat(result).isEqualTo(AuthenticationProvider.Montagu)

        fakeConfig = mock {
            on { get("auth.provider") } doReturn "montagu"
        }
        sut = AuthenticationConfig(fakeConfig)
        result = sut.getConfiguredProvider()

        assertThat(result).isEqualTo(AuthenticationProvider.Montagu)

        fakeConfig = mock {
            on { get("auth.provider") } doReturn "github"
        }
        sut = AuthenticationConfig(fakeConfig)
        result = sut.getConfiguredProvider()

        assertThat(result).isEqualTo(AuthenticationProvider.GitHub)
    }

    @Test
    fun `getConfiguredProvider throws error if provider is not GitHub or Montagu`()
    {
        val fakeConfig = mock<Config> {
            on { get("auth.provider") } doReturn "nonsense"
        }
        val sut = AuthenticationConfig(fakeConfig)

        assertThatThrownBy { sut.getConfiguredProvider() }
                .isInstanceOf(UnknownAuthenticationProvider::class.java)
    }
}
