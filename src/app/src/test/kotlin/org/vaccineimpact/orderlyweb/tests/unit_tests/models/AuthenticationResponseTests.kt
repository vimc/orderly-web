package org.vaccineimpact.orderlyweb.tests.unit_tests.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.AuthenticationResponse
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class AuthenticationResponseTests: TeamcityTests()
{
    @Test
    fun `can create`()
    {
        val sut = AuthenticationResponse("testTokenType", "testAccessToken", 1000)
        assertThat(sut.tokenType).isEqualTo("testTokenType")
        assertThat(sut.accessToken).isEqualTo("testAccessToken")
        assertThat(sut.expiresIn).isEqualTo(1000)
    }

    @Test
    fun `can create with default tokenType`()
    {
        val sut = AuthenticationResponse(accessToken = "testAccessToken", expiresIn = 1000)
        assertThat(sut.tokenType).isEqualTo("bearer")
    }
}