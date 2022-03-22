package org.vaccineimpact.orderlyweb.tests.security.clients

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.credentials.extractor.HeaderExtractor
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.security.authentication.GithubAuthenticator
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator
import org.vaccineimpact.orderlyweb.security.clients.GitHubDirectClient

class GithubDirectClientTests
{
    @Test
    fun `initialises as expected`()
    {
        val sut = GitHubDirectClient()
        sut.init()

        val credentialsExtractor = sut.credentialsExtractor
        assertThat(credentialsExtractor is HeaderExtractor).isTrue()
        assertThat((credentialsExtractor as HeaderExtractor).headerName).isEqualTo("Authorization")
        assertThat(credentialsExtractor.prefixHeader).isEqualTo("token ")

        val authenticator = sut.authenticator
        assertThat(authenticator is GithubAuthenticator).isTrue()

        val ags = sut.authorizationGenerators
        Assertions.assertThat((ags as List<AuthorizationGenerator<CommonProfile>>).count()).isEqualTo(1)
        assertThat(ags[0] is OrderlyAuthorizationGenerator).isTrue()
    }
}
