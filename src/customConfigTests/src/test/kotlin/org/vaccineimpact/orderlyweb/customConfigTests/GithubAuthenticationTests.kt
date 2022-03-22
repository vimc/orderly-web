package org.vaccineimpact.orderlyweb.customConfigTests

import com.github.fge.jackson.JsonLoader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.test_helpers.JSONValidator
import org.vaccineimpact.orderlyweb.test_helpers.TestTokenHeader
import org.vaccineimpact.orderlyweb.test_helpers.http.HttpClient
import org.vaccineimpact.orderlyweb.test_helpers.http.Response

class GithubAuthenticationTests : CustomConfigTests()
{
    val JSONValidator = JSONValidator()

    // this is a PAT for a test user who only has access to a test org with no repos
    // reversed so GitHub doesn't spot it and invalidate it
    val testUserPAT = "fcef1c6821f7561259ce45d4840965642607e5a4".reversed()

    @Test
    fun `authentication fails without Auth header`()
    {
        startApp("auth.provider=github")
        val result = HttpClient.post(url)

        assertAuthFailure(result)
    }

    @Test
    fun `authentication fails with malformed Auth header`()
    {
        startApp("auth.provider=github")
        val result = HttpClient.post(url, auth = TestTokenHeader("token", "bearer"))

        assertAuthFailure(result)
    }

    @Test
    fun `authentication fails with invalid github token`()
    {
        startApp("auth.provider=github")
        val result = HttpClient.post(url, auth = TestTokenHeader("badtoken"))

        assertAuthFailure(result)
    }

    @Test
    fun `authentication succeeds with well-formed Auth header`()
    {
        startApp("auth.provider=github")

        val result = HttpClient.post(url, auth = TestTokenHeader(testUserPAT))

        assertAuthSuccess(result)
    }

    @Test
    fun `authentication fails if token does not have email reading scope`()
    {
        startApp("auth.provider=github")
        // this is a PAT for a test user who only has access to a test org with no repos
        // reversed so GitHub doesn't spot it and invalidate it
        val tokenWithoutEmailReadingScope = "e0182507b0c6ad077a3036fd181a6260c0376e1c".reversed()

        val result = HttpClient.post(url, auth = TestTokenHeader(tokenWithoutEmailReadingScope))

        assertAuthFailure(result)
    }

    @Test
    fun `authentication fails if token does not have user reading scope`()
    {
        startApp("auth.provider=github")
        // this is a PAT for a test user who only has access to a test org with no repos
        // reversed so GitHub doesn't spot it and invalidate it
        val tokenWithoutUserReadingScope = "285d9b1b6620ab4dfd6c403b29451d52aa38a158".reversed()

        val result = HttpClient.post(url, auth = TestTokenHeader(tokenWithoutUserReadingScope))

        assertAuthFailure(result)
    }

    @Test
    fun `authentication fails if user is not in configured org`()
    {
        startApp("auth.provider=github\nauth.github_org=vimc")
        val result = HttpClient.post(url, auth = TestTokenHeader(testUserPAT))

        assertAuthFailure(result)
    }

    @Test
    fun `authentication succeeds if user is in configured team`()
    {
        startApp("auth.provider=github\nauth.github_team=vimc-auth-team")
        val result = HttpClient.post(url, auth = TestTokenHeader(testUserPAT))

        assertAuthSuccess(result)
    }

    @Test
    fun `authentication fails if user is not in configured team`()
    {
        startApp("auth.provider=github\nauth.github_team=vimc-auth-team2")
        val result = HttpClient.post(url, auth = TestTokenHeader(testUserPAT))

        assertAuthFailure(result)
    }

    @Test
    fun `can get OPTIONS for authentication endpoint`()
    {
        startApp("auth.provider=github")
        val result = HttpClient.options(url)
        assertThat(result.statusCode).isEqualTo(200)
    }

    private fun assertAuthSuccess(result: Response)
    {
        assertSuccessful(result)

        val json = JsonLoader.fromString(result.text)
        assertThat(json["token_type"].textValue()).isEqualTo("bearer")
        assertThat(json["access_token"]).isNotNull
        assertThat(isLong(json["expires_in"].toString())).isTrue()
    }

    private fun assertAuthFailure(result: Response)
    {
        assertThat(result.statusCode).isEqualTo(401)
        JSONValidator.validateError(result.text,
                expectedErrorCode = "github-token-invalid",
                expectedErrorText = "GitHub token not supplied in Authorization header, or GitHub token was invalid")
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
