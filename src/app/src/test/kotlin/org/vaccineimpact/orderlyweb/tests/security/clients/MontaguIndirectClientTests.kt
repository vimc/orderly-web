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
import org.vaccineimpact.orderlyweb.security.clients.MontaguLogoutActionBuilder
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class MontaguIndirectClientTests : TeamcityTests()
{
    @Test
    fun `initialises as expected`()
    {
        val sut = MontaguIndirectClient()
        sut.init()

        val extractor = sut.credentialsExtractor
        assertThat(extractor is CookieExtractor).isTrue()
        assertThat(extractor.toString()).contains("cookieName: montagu_jwt_token")

        val redirectActionBuilder = sut.redirectActionBuilder
        assertThat(redirectActionBuilder is MontaguIndirectClientRedirectActionBuilder).isTrue()

        val authenticator = sut.authenticator
        assertThat(authenticator is MontaguAuthenticator).isTrue()

        val ags = sut.authorizationGenerators
        Assertions.assertThat((ags as List<AuthorizationGenerator<CommonProfile>>).count()).isEqualTo(1)
        assertThat(ags[0] is OrderlyAuthorizationGenerator).isTrue()

        assertThat(sut.callbackUrl).isEqualTo("/login")

        assertThat(sut.logoutActionBuilder is MontaguLogoutActionBuilder).isTrue()

    }
}