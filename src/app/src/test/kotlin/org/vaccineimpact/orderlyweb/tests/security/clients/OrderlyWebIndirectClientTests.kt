package org.vaccineimpact.orderlyweb.tests.security.clients

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.client.BaseClient
import org.pac4j.core.client.IndirectClient
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.security.clients.OrderlyWebIndirectClient
import org.vaccineimpact.orderlyweb.security.clients.NeverInvokedCredentialsExtractor
import org.vaccineimpact.orderlyweb.security.clients.NeverInvokedAuthenticator
import org.vaccineimpact.orderlyweb.security.clients.OrderlyWebIndirectClientRedirectActionBuilder

class OrderlyWebIndirectClientTests : TeamcityTests()
{
    @Test
    fun `initialises as expected`()
    {
        val sut = OrderlyWebIndirectClient()
        sut.init()

        //inherited private credentialsExtractor field should be NeverInvokedCredentialsExtractor
        val baseClientClass = BaseClient::class.java
        val extrField = baseClientClass.getDeclaredField("credentialsExtractor")
        extrField.isAccessible = true
        val credentialsExtractor = extrField.get(sut)
        assertThat(credentialsExtractor is NeverInvokedCredentialsExtractor).isTrue()

        //inherited private authenticator field should be NeverInvokedAuthenticator
        val authField = baseClientClass.getDeclaredField("authenticator")
        authField.isAccessible = true
        val authenticator = authField.get(sut)
        assertThat(authenticator is NeverInvokedAuthenticator).isTrue()

        //inherited private redirectActionBuilder field should be OrderlyWebIndirectClientRedirectActionBuilder
        val indirectClientClass = IndirectClient::class.java
        val rabField = indirectClientClass.getDeclaredField("redirectActionBuilder")
        rabField.isAccessible = true
        val redirectActionBuilder = rabField.get(sut)
        assertThat(redirectActionBuilder is OrderlyWebIndirectClientRedirectActionBuilder).isTrue()

        //inherited private callbackUrl field should be "/login"
        val callbackUrlField = indirectClientClass.getDeclaredField("callbackUrl")
        callbackUrlField.isAccessible = true
        assertThat(callbackUrlField.get(sut)).isEqualTo("/login")
    }
}