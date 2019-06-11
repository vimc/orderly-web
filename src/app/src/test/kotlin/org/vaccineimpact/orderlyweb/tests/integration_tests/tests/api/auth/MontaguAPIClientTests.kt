package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api.auth

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.security.providers.MontaguAPIException
import org.vaccineimpact.orderlyweb.security.providers.khttpMontaguAPIClient
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.APIRequestHelper

class MontaguAPIClientTests : TeamcityTests()
{
    @Test
    fun `khttpMontaguAPIClient can talk to API`()
    {
        val token = login()["access_token"].toString()
        val sut = khttpMontaguAPIClient()
        sut.getUserDetails(token)
    }

    @Test
    fun `khttpMontaguAPIClient throws error if request fails`()
    {
        val sut = khttpMontaguAPIClient()

        assertThatThrownBy {
            sut.getUserDetails("bad-token")
        }.isInstanceOf(MontaguAPIException::class.java)
    }


    @Test
    fun `khttpMontaguAPIClient can get user details`()
    {
        val token = login()["access_token"].toString()
        val sut = khttpMontaguAPIClient()
        val result = sut.getUserDetails(token)
        Assertions.assertThat(result.username).isEqualTo("test.user")
        Assertions.assertThat(result.email).isEqualTo("test.user@example.com")
        Assertions.assertThat(result.name).isEqualTo("Test User")
    }

    private fun login() = APIRequestHelper().loginWithMontagu()

}