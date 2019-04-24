package org.vaccineimpact.orderlyweb.tests.security.RedirectActionBuilders

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat
import org.pac4j.core.context.WebContext
import org.pac4j.core.redirect.RedirectAction
import org.vaccineimpact.orderlyweb.security.clients.OrderlyWebIndirectClientRedirectActionBuilder
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class OrderlyWebIndirectClientRedirectActionBuilderTests: TeamcityTests()
{
    @Test
    fun `returns expected RedirectAction`()
    {
        val sut = OrderlyWebIndirectClientRedirectActionBuilder()

        val mockWebContext = mock<WebContext> {
            on(it.fullRequestURL) doReturn "http://localhost:8888/reports/1"
        }

        val action = sut.redirect(mockWebContext)
        val expectedUrl = "http://localhost:8888/weblogin?requestedUrl=http%3A%2F%2Flocalhost%3A8888%2Freports%2F1"
        assertThat(action.location).isEqualTo(expectedUrl)
        assertThat(action.type).isEqualTo(RedirectAction.RedirectType.REDIRECT)
    }
}