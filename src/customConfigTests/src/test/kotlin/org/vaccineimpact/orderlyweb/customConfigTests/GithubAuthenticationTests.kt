package org.vaccineimpact.orderlyweb.customConfigTests

import com.github.fge.jackson.JsonLoader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.vaccineimpact.orderlyweb.test_helpers.JSONValidator
import org.vaccineimpact.orderlyweb.test_helpers.TestTokenHeader
import org.vaccineimpact.orderlyweb.test_helpers.http.HttpClient

class GithubAuthenticationTests : CustomConfigTests()
{
    val JSONValidator = JSONValidator()

    @Before
    fun start() {
        startApp("auth.provider=github")
    }

    @Test
    fun `authentication fails without Auth header`()
    {
        val result = HttpClient.post(url)
        assertThat(result.statusCode).isEqualTo(401)
        JSONValidator.validateError(result.text,
                expectedErrorCode = "github-token-invalid",
                expectedErrorText = "GitHub token not supplied in Authorization header, or GitHub token was invalid")
    }

    @Test
    fun `authentication fails with malformed Auth header`()
    {
        val result = HttpClient.post(url, auth = TestTokenHeader("token", "bearer"))
        assertThat(result.statusCode).isEqualTo(401)
        JSONValidator.validateError(result.text,
                expectedErrorCode = "github-token-invalid",
                expectedErrorText = "GitHub token not supplied in Authorization header, or GitHub token was invalid")
    }

    @Test
    fun `authentication fails with invalid github token`()
    {
        val result = HttpClient.post(url, auth = TestTokenHeader("badtoken"))
        assertThat(result.statusCode).isEqualTo(401)
        JSONValidator.validateError(result.text,
                expectedErrorCode = "github-token-invalid",
                expectedErrorText = "GitHub token not supplied in Authorization header, or GitHub token was invalid")
    }

    @Test
    fun `authentication succeeds with well-formed Auth header`()
    {
        // this is a PAT for a test user who only has access to a test org with no repos
        // reversed so GitHub doesn't spot it and invalidate it
        val token = "fcef1c6821f7561259ce45d4840965642607e5a4".reversed()

        val result = HttpClient.post(url, auth = TestTokenHeader(token))

        assertSuccessful(result)

        val json = JsonLoader.fromString(result.text)
        assertThat(json["token_type"].textValue()).isEqualTo("bearer")
        assertThat(json["access_token"]).isNotNull
        assertThat(isLong(json["expires_in"].toString())).isTrue()
    }

    @Test
    fun `authentication fails if token does not have email reading scope`()
    {
        // this is a PAT for a test user who only has access to a test org with no repos
        // reversed so GitHub doesn't spot it and invalidate it
        val tokenWithoutEmailReadingScope = "e0182507b0c6ad077a3036fd181a6260c0376e1c".reversed()

        val result = HttpClient.post(url, auth = TestTokenHeader(tokenWithoutEmailReadingScope))

        JSONValidator.validateError(result.text,
                expectedErrorCode = "github-token-invalid",
                expectedErrorText = "GitHub token not supplied in Authorization header, or GitHub token was invalid")

    }

    @Test
    fun `authentication fails if token does not have user reading scope`()
    {
        // this is a PAT for a test user who only has access to a test org with no repos
        // reversed so GitHub doesn't spot it and invalidate it
        val tokenWithoutUserReadingScope = "285d9b1b6620ab4dfd6c403b29451d52aa38a158".reversed()

        val result = HttpClient.post(url, auth = TestTokenHeader(tokenWithoutUserReadingScope))

        JSONValidator.validateError(result.text,
                expectedErrorCode = "github-token-invalid",
                expectedErrorText = "GitHub token not supplied in Authorization header, or GitHub token was invalid")

    }

    @Test
    fun `can get OPTIONS for authentication endpoint`()
    {
        val result = HttpClient.options(url)
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

    val url = "${RequestHelper.apiBaseUrl}/login/"
}