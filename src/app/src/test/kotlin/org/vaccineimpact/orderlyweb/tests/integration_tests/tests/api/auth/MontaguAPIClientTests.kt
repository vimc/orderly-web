package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api.auth

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.security.providers.MontaguAPIException
import org.vaccineimpact.orderlyweb.security.providers.OkhttpMontaguAPIClient
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.APIRequestHelper

class MontaguAPIClientTests : TeamcityTests()
{
    @Test
    fun `okhttpMontaguAPIClient can talk to API`()
    {
        val token = login()["access_token"].toString()
        val sut = OkhttpMontaguAPIClient()
        sut.getUserDetails(token)
    }

    @Test
    fun `okhttpMontaguAPIClient throws error if request fails`()
    {
        val sut = OkhttpMontaguAPIClient()

        assertThatThrownBy {
            sut.getUserDetails("bad-token")
        }.isInstanceOf(MontaguAPIException::class.java)
    }


    @Test
    fun `okhttpMontaguAPIClient can get user details`()
    {
        val token = login()["access_token"].toString()
        val sut = OkhttpMontaguAPIClient()
        val result = sut.getUserDetails(token)
        Assertions.assertThat(result.username).isEqualTo("test.user")
        Assertions.assertThat(result.email).isEqualTo("test.user@example.com")
        Assertions.assertThat(result.name).isEqualTo("Test User")
    }

    @Test
    fun `okhttpMontaguAPIClient can get user details in proxy dev mode`()
    {
        val mockConfig = mock<Config>{
            on { get("proxy.dev.mode") } doReturn "true"
            on { get("montagu.api_url") } doReturn AppConfig()["montagu.api_url"]
        }

        val token = login()["access_token"].toString()
        val sut = OkhttpMontaguAPIClient(appConfig = mockConfig)
        val result = sut.getUserDetails(token)
        Assertions.assertThat(result.username).isEqualTo("test.user")
    }

    private fun login() = APIRequestHelper().loginWithMontagu()

}