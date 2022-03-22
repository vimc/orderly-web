package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.api.OnetimeTokenController
import org.vaccineimpact.orderlyweb.db.OnetimeTokenStore
import org.vaccineimpact.orderlyweb.errors.MissingParameterError

class OnetimeTokenControllerTests : ControllerTest()
{
    @Test
    fun `can get token`()
    {
        val mockUserProfile = mock<CommonProfile> {
            on { it.id } doReturn "testuserid@example.com"
        }

        val mockContext = mock<ActionContext> {
            on { it.queryParams("url") } doReturn "testUrl"
            on { it.userProfile } doReturn mockUserProfile
        }
        val mockTokenStore = mock<OnetimeTokenStore> {}

        val sut = OnetimeTokenController(mockContext, mockTokenStore)
        val token = sut.get()

        verify(mockTokenStore).storeToken(token)
    }

    @Test
    fun `throws MissingParameterError when url param is absent`()
    {
        val sut = OnetimeTokenController(mock(), mock())

        assertThatThrownBy { sut.get() }.isInstanceOf(MissingParameterError::class.java)
    }
}
