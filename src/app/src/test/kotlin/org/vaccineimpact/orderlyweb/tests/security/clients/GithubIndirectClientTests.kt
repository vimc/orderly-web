package org.vaccineimpact.orderlyweb.tests.security.clients

import org.assertj.core.api.Assertions
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.client.BaseClient
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.security.authentication.GithubOAuthAuthenticator
import org.vaccineimpact.orderlyweb.security.authentication.GithubOAuthProfileCreator
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator
import org.vaccineimpact.orderlyweb.security.clients.GithubIndirectClient
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class GithubIndirectClientTests : TeamcityTests()
{
    @Test
    fun `initialises as expected`()
    {
        val sut = GithubIndirectClient("testkey", "testsecret")
        sut.init()

        //base class should be GithubClient
        assertThat(sut.javaClass.superclass.name).isEqualTo("org.pac4j.oauth.client.GitHubClient")

        //inherited private authenticator field should be GithubOAuthAuthenticator
        val baseClientClass = BaseClient::class.java
        val authField = baseClientClass.getDeclaredField("authenticator")
        authField.isAccessible = true
        val authenticator = authField.get(sut)
        assertThat(authenticator is GithubOAuthAuthenticator).isTrue()

        //inherited protected profileCreator field should be GithubOAuthProfileCreator
        val pcField = baseClientClass.getDeclaredField("profileCreator")
        val profileCreator = pcField.get(sut)
        assertThat(profileCreator is GithubOAuthProfileCreator).isTrue()

        //inherited private authorizationGenerators field should include OrderlyAuthorizationGenerator
        val agsField = baseClientClass.getDeclaredField("authorizationGenerators")
        agsField.isAccessible = true
        val ags = agsField.get(sut)
        Assertions.assertThat((ags as List<AuthorizationGenerator<CommonProfile>>).count()).isEqualTo(1)
        assertThat(ags[0] is OrderlyAuthorizationGenerator).isTrue()

        //inherited private callbackUrl field should be set
        val indirectClientClass = IndirectClient::class.java
        val callbackUrlField = indirectClientClass.getDeclaredField("callbackUrl")
        callbackUrlField.isAccessible = true
        assertThat(callbackUrlField.get(sut)).isEqualTo("http://localhost:8888/login")

    }
}