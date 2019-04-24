package org.vaccineimpact.orderlyweb.tests.security.clients

import org.assertj.core.api.Assertions
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.client.BaseClient
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.credentials.extractor.CookieExtractor
import org.vaccineimpact.orderlyweb.security.authentication.MontaguAuthenticator
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClient
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClientRedirectActionBuilder
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class MontaguIndirectClientTests : TeamcityTests()
{
    @Test
    fun `initialises as expected`()
    {
        val sut = MontaguIndirectClient()
        sut.init()

        //inherited private credentialsExtractor field should be CookieExtractor
        val baseClientClass = BaseClient::class.java
        val extrField = baseClientClass.getDeclaredField("credentialsExtractor")
        extrField.isAccessible = true
        val extractor = extrField.get(sut)
        assertThat(extractor is CookieExtractor).isTrue()
        val cookieExtractorClass = CookieExtractor::class.java
        val cookieNameField = cookieExtractorClass.getDeclaredField("cookieName")
        cookieNameField.isAccessible = true
        assertThat(cookieNameField.get(extractor)).isEqualTo("montagu_jwt_token")

        //inherited private redirectActionBuilder field should be MontaguIndirectClientRedirectActionBuilder
        val indirectClientClass = IndirectClient::class.java
        val rabField = indirectClientClass.getDeclaredField("redirectActionBuilder")
        rabField.isAccessible = true
        val redirectActionBuilder = rabField.get(sut)
        assertThat(redirectActionBuilder is MontaguIndirectClientRedirectActionBuilder).isTrue()

        //inherited private authenticator field should be MontaguAuthenticator
        val authField = baseClientClass.getDeclaredField("authenticator")
        authField.isAccessible = true
        val authenticator = authField.get(sut)
        assertThat(authenticator is MontaguAuthenticator).isTrue()

        //inherited private authorizationGenerators field should include OrderlyAuthorizationGenerator
        val agsField = baseClientClass.getDeclaredField("authorizationGenerators")
        agsField.isAccessible = true
        val ags = agsField.get(sut)
        Assertions.assertThat((ags as List<AuthorizationGenerator<CommonProfile>>).count()).isEqualTo(1)
        assertThat(ags[0] is OrderlyAuthorizationGenerator).isTrue()

        //inherited private callbackUrl field should be set
        val callbackUrlField = indirectClientClass.getDeclaredField("callbackUrl")
        callbackUrlField.isAccessible = true
        assertThat(callbackUrlField.get(sut)).isEqualTo("/login")

    }
}