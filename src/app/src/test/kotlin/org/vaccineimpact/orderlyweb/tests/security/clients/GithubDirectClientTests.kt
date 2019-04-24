package org.vaccineimpact.orderlyweb.tests.security.clients

import org.assertj.core.api.Assertions
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.client.BaseClient
import org.pac4j.core.credentials.extractor.HeaderExtractor
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.security.authentication.GithubAuthenticator
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator
import org.vaccineimpact.orderlyweb.security.clients.GithubDirectClient
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class GithubDirectClientTests : TeamcityTests()
{
    @Test
    fun `initialises as expected`()
    {
        val sut = GithubDirectClient()
        sut.init()

        //inherited private credentialsExtractor field should be HeaderExtractor
        val baseClientClass = BaseClient::class.java
        val extrField = baseClientClass.getDeclaredField("credentialsExtractor")
        extrField.isAccessible = true
        val credentialsExtractor = extrField.get(sut)
        assertThat(credentialsExtractor is HeaderExtractor).isTrue()
        assertThat((credentialsExtractor as HeaderExtractor).headerName).isEqualTo("Authorization")
        assertThat(credentialsExtractor.prefixHeader).isEqualTo("token ")

        //inherited private authenticator field should be GithubAuthenticator
        val authField = baseClientClass.getDeclaredField("authenticator")
        authField.isAccessible = true
        val authenticator = authField.get(sut)
        assertThat(authenticator is GithubAuthenticator).isTrue()

        //inherited private authorizationGenerators field should include OrderlyAuthorizationGenerator
        val agsField = baseClientClass.getDeclaredField("authorizationGenerators")
        agsField.isAccessible = true
        val ags = agsField.get(sut)
        Assertions.assertThat((ags as List<AuthorizationGenerator<CommonProfile>>).count()).isEqualTo(1)
        assertThat(ags[0] is OrderlyAuthorizationGenerator).isTrue()

    }
}