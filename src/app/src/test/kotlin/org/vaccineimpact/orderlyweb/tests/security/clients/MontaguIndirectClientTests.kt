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
import org.vaccineimpact.orderlyweb.security.clients.MontaguLogoutActionBuilder
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class MontaguIndirectClientTests : TeamcityTests()
{
    @Test
    fun `initialises as expected`()
    {
        val sut = MontaguIndirectClient()
        sut.init()

        //inherited private credentialsExtractor field should be CookieExtractor
        val extractor = sut.credentialsExtractor
        assertThat(extractor is CookieExtractor).isTrue()
        assertThat(extractor.toString()).isEqualTo("montagu_jwt_token")

        //inherited private authenticator field should be MontaguAuthenticator
        val authenticator = sut.authenticator
        assertThat(authenticator is MontaguAuthenticator).isTrue()

        //inherited private authorizationGenerators field should include OrderlyAuthorizationGenerator
        val ags = sut.authorizationGenerators
        Assertions.assertThat((ags as List<AuthorizationGenerator<CommonProfile>>).count()).isEqualTo(1)
        assertThat(ags[0] is OrderlyAuthorizationGenerator).isTrue()

        //inherited private callbackUrl field should be set
        assertThat(sut.callbackUrl).isEqualTo("/login")

        //inherited logoutActionBuilder should be MontaguLogoutActionBuilder
        assertThat(sut.logoutActionBuilder is MontaguLogoutActionBuilder).isTrue()

    }
}