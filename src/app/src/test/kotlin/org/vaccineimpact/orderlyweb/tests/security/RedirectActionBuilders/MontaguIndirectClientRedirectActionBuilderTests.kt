package org.vaccineimpact.orderlyweb.tests.security.RedirectActionBuilders

import com.nhaarman.mockito_kotlin.mock
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat
import org.pac4j.core.redirect.RedirectAction
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClientRedirectActionBuilder
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class MontaguIndirectClientRedirectActionBuilderTests: TeamcityTests()
{
    @Test
    fun `returns expected RedirectAction`()
    {
        val sut = MontaguIndirectClientRedirectActionBuilder()
        val action = sut.redirect(mock())
        val expectedUrl = "http://localhost?redirectTo=http%3A%2F%2Flocalhost%3A8888%2Flogin"
        assertThat(action.location).isEqualTo(expectedUrl)
        assertThat(action.type).isEqualTo(RedirectAction.RedirectType.REDIRECT)
    }
}