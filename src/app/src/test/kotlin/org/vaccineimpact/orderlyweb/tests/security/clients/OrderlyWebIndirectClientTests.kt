package org.vaccineimpact.orderlyweb.tests.security.clients

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.client.BaseClient
import org.pac4j.core.client.IndirectClient
import org.vaccineimpact.orderlyweb.security.clients.OrderlyWebIndirectClient
import org.vaccineimpact.orderlyweb.security.clients.NeverInvokedCredentialsExtractor
import org.vaccineimpact.orderlyweb.security.clients.NeverInvokedAuthenticator
import org.vaccineimpact.orderlyweb.security.clients.OrderlyWebIndirectClientRedirectActionBuilder

class OrderlyWebIndirectClientTests
{
    @Test
    fun `initialises as expected`()
    {
        val sut = OrderlyWebIndirectClient()
        sut.init()

        val credentialsExtractor = sut.credentialsExtractor
        assertThat(credentialsExtractor is NeverInvokedCredentialsExtractor).isTrue()

        val authenticator = sut.authenticator
        assertThat(authenticator is NeverInvokedAuthenticator).isTrue()

        val redirectActionBuilder = sut.redirectActionBuilder
        assertThat(redirectActionBuilder is OrderlyWebIndirectClientRedirectActionBuilder).isTrue()

        assertThat(sut.callbackUrl).isEqualTo("/login")
    }
}