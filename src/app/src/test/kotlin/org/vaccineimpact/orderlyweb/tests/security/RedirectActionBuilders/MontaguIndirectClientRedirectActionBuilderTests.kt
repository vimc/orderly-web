package org.vaccineimpact.orderlyweb.tests.security.RedirectActionBuilders

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.redirect.RedirectAction
import org.pac4j.http.credentials.extractor.CookieExtractor
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClientRedirectActionBuilder
import org.vaccineimpact.orderlyweb.security.providers.MontaguAPIClient
import org.vaccineimpact.orderlyweb.security.providers.MontaguAPIException

class MontaguIndirectClientRedirectActionBuilderTests
{
    @Test
    fun `redirects user to Montagu if can't extract cookie`()
    {
        val mockCookieExtractor = mock<CookieExtractor> {
            on { extract(any()) } doReturn null as TokenCredentials?
        }

        val sut = MontaguIndirectClientRedirectActionBuilder(mock(), mockCookieExtractor)
        val action = sut.redirect(mock())
        val expectedUrl = "http://localhost?redirectTo=http%3A%2F%2Flocalhost%3A8888%2Flogin"
        assertThat(action.location).isEqualTo(expectedUrl)
        assertThat(action.type).isEqualTo(RedirectAction.RedirectType.REDIRECT)
    }

    @Test
    fun `redirects user to Montagu if can't retrieve Montagu user details`()
    {
        val mockCookieExtractor = mock<CookieExtractor> {
            on { extract(any()) } doReturn TokenCredentials("token")
        }

        val mockMontaguAPIClient = mock<MontaguAPIClient> {
            on {getUserDetails("token")} doThrow MontaguAPIException("some error", 401)
        }

        val sut = MontaguIndirectClientRedirectActionBuilder(mockMontaguAPIClient, mockCookieExtractor)
        val action = sut.redirect(mock())
        val expectedUrl = "http://localhost?redirectTo=http%3A%2F%2Flocalhost%3A8888%2Flogin"
        assertThat(action.location).isEqualTo(expectedUrl)
        assertThat(action.type).isEqualTo(RedirectAction.RedirectType.REDIRECT)
    }

    @Test
    fun `redirects user to login callback if can retrieve Montagu user details`()
    {
        val mockCookieExtractor = mock<CookieExtractor>() {
            on { extract(any()) } doReturn TokenCredentials("token")
        }

        val sut = MontaguIndirectClientRedirectActionBuilder(mock(), mockCookieExtractor)
        val action = sut.redirect(mock())
        val expectedUrl = "http://localhost:8888/login"
        assertThat(action.location).isEqualTo(expectedUrl)
        assertThat(action.type).isEqualTo(RedirectAction.RedirectType.REDIRECT)
    }
}