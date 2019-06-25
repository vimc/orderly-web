package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api.auth

import com.github.fge.jackson.JsonLoader
import khttp.options
import khttp.post
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.test_helpers.TestTokenHeader
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.APIRequestHelper
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class MontaguAuthenticationTests : IntegrationTest()
{
    @Test
    fun `authentication fails without Auth header`()
    {
        val result = post(url)
        assertThat(result.statusCode).isEqualTo(401)
        JSONValidator.validateError(result.text,
                expectedErrorCode = "montagu-token-invalid",
                expectedErrorText = "Montagu token not supplied in Authorization header, or Montagu token was invalid")
    }

    @Test
    fun `authentication fails with malformed Auth header`()
    {
        val result = post(url, auth = TestTokenHeader("token", "wrongprefix"))
        assertThat(result.statusCode).isEqualTo(401)
        JSONValidator.validateError(result.text,
                expectedErrorCode = "montagu-token-invalid",
                expectedErrorText = "Montagu token not supplied in Authorization header, or Montagu token was invalid")
    }

    @Test
    fun `authentication fails with invalid github token`()
    {
        val result = post(url, auth = TestTokenHeader("badtoken"))
        assertThat(result.statusCode).isEqualTo(401)
        JSONValidator.validateError(result.text,
                expectedErrorCode = "montagu-token-invalid",
                expectedErrorText = "Montagu token not supplied in Authorization header, or Montagu token was invalid")
    }

    @Test
    fun `authentication succeeds with well-formed Auth header`()
    {
        val token = APIRequestHelper().loginWithMontagu()["access_token"].toString()

        val result = post(url, auth = TestTokenHeader(token))

        assertSuccessful(result)

        val json = JsonLoader.fromString(result.text)
        assertThat(json["token_type"].textValue()).isEqualTo("bearer")
        assertThat(json["access_token"]).isNotNull
        assertThat(isLong(json["expires_in"].toString())).isTrue()
    }

    @Test
    fun `can get OPTIONS for authentication endpoint`()
    {
        val result = options(url)
        assertThat(result.statusCode).isEqualTo(200)
    }

    private fun isLong(raw: String): Boolean
    {
        try
        {
            raw.toLong()
            return true
        }
        catch (e: NumberFormatException)
        {
            return false
        }
    }

    val url = "${APIRequestHelper().baseUrl}/login/"
}