package org.vaccineimpact.orderlyweb.tests.security.LogoutActionBuilders

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.pac4j.core.context.WebContext
import org.pac4j.core.exception.http.FoundAction
import org.vaccineimpact.orderlyweb.security.clients.MontaguLogoutActionBuilder

class MontaguLogoutActionBuilderTests
{
    @Test
    fun `returns expected redirect action`()
    {
        val mockContext = mock<WebContext> {
            on { serverName } doReturn "testServer"
        }

        val sut = MontaguLogoutActionBuilder()
        val result = sut.getLogoutAction(mockContext, mock(), mock(), "/")

        assertThat((result.get() as FoundAction).location)
                .isEqualTo("http://localhost:8888")
    }

}
