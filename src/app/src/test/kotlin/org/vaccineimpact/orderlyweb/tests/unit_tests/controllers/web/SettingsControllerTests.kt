package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.SettingsController
import org.vaccineimpact.orderlyweb.db.repositories.SettingsRepository
import org.vaccineimpact.orderlyweb.errors.InvalidOperationError
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class SettingsControllerTests : TeamcityTests()
{
    val mockAuthConfig = mock<AuthenticationConfig>{
        on { canAllowGuestUser } doReturn true
        on { allowGuestUser } doReturn true
    }

    @Test
    fun `getAuthAllowGuest gets setting from config object`()
    {
        val sut = SettingsController(mock(), mock(), mockAuthConfig)

        assertThat(sut.getAuthAllowGuest()).isTrue()
    }

    @Test
    fun `setAuthAllowGuest sets setting in database`()
    {
        val mockRepo = mock<SettingsRepository>()
        val mockContext = mock<ActionContext>{
            on {getRequestBody()} doReturn "true"
        }

        val sut = SettingsController(mockContext, mockRepo, mockAuthConfig)
        sut.setAuthAllowGuest()

        verify(mockRepo).setAuthAllowGuest(true)
    }

    @Test
    fun `setAuthAllowGuest throws InvalidOperationError if cannot allow guest user`()
    {
        val cannotAllowConfig = mock<AuthenticationConfig>{
            on { canAllowGuestUser } doReturn false
        }

        val sut = SettingsController(mock(), mock(), cannotAllowConfig)
        assertThatThrownBy{ sut.setAuthAllowGuest() }
                .isInstanceOf(InvalidOperationError::class.java)
                .hasMessageContaining("Cannot set auth-allow-guest with current application configuration")
    }
}