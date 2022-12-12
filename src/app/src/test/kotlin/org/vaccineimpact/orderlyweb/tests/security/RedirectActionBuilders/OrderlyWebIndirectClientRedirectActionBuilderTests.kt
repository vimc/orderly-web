package org.vaccineimpact.orderlyweb.tests.security.RedirectActionBuilders

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.pac4j.core.context.HttpConstants
import org.pac4j.core.context.WebContext
import org.pac4j.core.exception.http.FoundAction
import org.vaccineimpact.orderlyweb.security.clients.OrderlyWebIndirectClientRedirectActionBuilder

class OrderlyWebIndirectClientRedirectActionBuilderTests
{
    @Test
    fun `returns expected RedirectAction`()
    {
        val sut = OrderlyWebIndirectClientRedirectActionBuilder()

        val mockWebContext = mock<WebContext> {
            on(it.fullRequestURL) doReturn "http://localhost:8888/reports/1"
        }

        val action = sut.getRedirectionAction(mockWebContext, mock()).get() as FoundAction
        val expectedUrl = "http://localhost:8888/weblogin?requestedUrl=http%3A%2F%2Flocalhost%3A8888%2Freports%2F1"
        assertThat(action.location).isEqualTo(expectedUrl)
        assertThat(action.code).isEqualTo(HttpConstants.FOUND)
    }
}