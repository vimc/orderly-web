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

        val authenticator = sut.authenticator
        assertThat(authenticator is GithubOAuthAuthenticator).isTrue()

        val profileCreator = sut.profileCreator
        assertThat(profileCreator is GithubOAuthProfileCreator).isTrue()

        val ags = sut.authorizationGenerators
        Assertions.assertThat(ags.count()).isEqualTo(1)
        assertThat(ags[0] is OrderlyAuthorizationGenerator).isTrue()

        assertThat(sut.callbackUrl).isEqualTo("http://localhost:8888/login")

    }
}