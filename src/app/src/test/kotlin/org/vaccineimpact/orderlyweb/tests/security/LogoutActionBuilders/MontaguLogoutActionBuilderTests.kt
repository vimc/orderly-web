package org.vaccineimpact.orderlyweb.tests.security.LogoutActionBuilders

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.pac4j.core.context.Cookie
import org.pac4j.core.context.WebContext
import org.vaccineimpact.orderlyweb.security.clients.MontaguLogoutActionBuilder
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class MontaguLogoutActionBuilderTests : TeamcityTests()
{
    @Test
    fun `returns expected redirect action`()
    {
        val mockContext = mock<WebContext> {
            on { serverName } doReturn "testServer"
        }

        val sut = MontaguLogoutActionBuilder()
        val result = sut.getLogoutAction(mockContext, mock(), "/")

        assertThat(result.location).isEqualTo("http://localhost:8888")
    }

}