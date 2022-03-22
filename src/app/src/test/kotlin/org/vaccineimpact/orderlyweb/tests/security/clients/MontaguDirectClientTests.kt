package org.vaccineimpact.orderlyweb.tests.security.clients

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.credentials.extractor.HeaderExtractor
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.security.authentication.MontaguAuthenticator
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator
import org.vaccineimpact.orderlyweb.security.clients.MontaguDirectClient

class MontaguDirectClientTests
{
    @Test
    fun `initialises as expected`()
    {
        val sut = MontaguDirectClient()
        sut.init()

        val credentialsExtractor = sut.credentialsExtractor
        assertThat(credentialsExtractor is HeaderExtractor).isTrue()
        assertThat((credentialsExtractor as HeaderExtractor).headerName).isEqualTo("Authorization")
        assertThat(credentialsExtractor.prefixHeader).isEqualTo("token ")

        val authenticator = sut.authenticator
        assertThat(authenticator is MontaguAuthenticator).isTrue()

        val ags = sut.authorizationGenerators
        Assertions.assertThat((ags as List<AuthorizationGenerator<CommonProfile>>).count()).isEqualTo(1)
        assertThat(ags[0] is OrderlyAuthorizationGenerator).isTrue()
    }
}
