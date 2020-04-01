package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.SettingsController
import org.vaccineimpact.orderlyweb.db.repositories.SettingsRepository
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class SettingsControllerTests : TeamcityTests()
{
    @Test
    fun `getAuthAllowGuest gets setting from database`()
    {
        val mockRepo = mock<SettingsRepository>{
            on { getAuthAllowGuest() } doReturn true
        }
        val sut = SettingsController(mock(), mockRepo)

        assertThat(sut.getAuthAllowGuest()).isTrue()
    }

    @Test
    fun `setAuthAllowGuest sets setting in database`()
    {
        val mockRepo = mock<SettingsRepository>()
        val mockContext = mock<ActionContext>{
            on {getRequestBody()} doReturn "true"
        }

        val sut = SettingsController(mockContext, mockRepo)
        sut.setAuthAllowGuest()

        verify(mockRepo).setAuthAllowGuest(true)
    }
}