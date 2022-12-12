package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api.auth

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.security.providers.MontaguAPIException
import org.vaccineimpact.orderlyweb.security.providers.OkHttpMontaguAPIClient
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.APIRequestHelper

class MontaguAPIClientTests
{
    @Test
    fun `okhttpMontaguAPIClient can talk to API`()
    {
        val token = login()["access_token"].toString()
        val sut = OkHttpMontaguAPIClient.create()
        sut.getUserDetails(token)
    }

    @Test
    fun `okhttpMontaguAPIClient throws error if request fails`()
    {
        val sut = OkHttpMontaguAPIClient.create()

        assertThatThrownBy {
            sut.getUserDetails("bad-token")
        }.isInstanceOf(MontaguAPIException::class.java)
    }


    @Test
    fun `okhttpMontaguAPIClient can get user details`()
    {
        val token = login()["access_token"].toString()
        val sut = OkHttpMontaguAPIClient.create()
        val result = sut.getUserDetails(token)
        Assertions.assertThat(result.username).isEqualTo("test.user")
        Assertions.assertThat(result.email).isEqualTo("test.user@example.com")
        Assertions.assertThat(result.name).isEqualTo("Test User")
    }

    @Test
    fun `okhttpMontaguAPIClient can get user details not allowing localhost`()
    {
        val mockConfig = mock<Config> {
            on { getBool("allow.localhost") } doReturn false
            on { get("montagu.api_url") } doReturn AppConfig()["montagu.api_url"]
        }

        val token = login()["access_token"].toString()
        val sut = OkHttpMontaguAPIClient.create(appConfig = mockConfig)
        val result = sut.getUserDetails(token)
        Assertions.assertThat(result.username).isEqualTo("test.user")
    }

    private fun login() = APIRequestHelper().loginWithMontagu()

}