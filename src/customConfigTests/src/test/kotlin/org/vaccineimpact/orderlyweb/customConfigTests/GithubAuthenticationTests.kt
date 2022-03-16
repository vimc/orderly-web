package org.vaccineimpact.orderlyweb.customConfigTests

import com.github.fge.jackson.JsonLoader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.vaccineimpact.orderlyweb.test_helpers.JSONValidator
import org.vaccineimpact.orderlyweb.test_helpers.TestTokenHeader
import org.vaccineimpact.orderlyweb.test_helpers.http.HttpClient
import org.vaccineimpact.orderlyweb.test_helpers.http.Response

class GithubAuthenticationTests : CustomConfigTests()
{
    val JSONValidator = JSONValidator()

    // Token with read:user, read:org and user:email perms, for a test user who only has access to a test org with no
    // repos. Token reversed so GitHub doesn't spot it and invalidate it
    val testUserPAT = "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"

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
        startApp("auth.provider=github")
        val result = HttpClient.post(url, auth = TestTokenHeader("token", "bearer"))
        assertThat(result.statusCode).isEqualTo(401)
        JSONValidator.validateError(result.text,
                expectedErrorCode = "github-token-invalid",
                expectedErrorText = "GitHub token not supplied in Authorization header, or GitHub token was invalid")
    }

    @Test
    fun `authentication fails with invalid github token`()
    {
        startApp("auth.provider=github")
        val result = HttpClient.post(url, auth = TestTokenHeader("badtoken"))
        assertThat(result.statusCode).isEqualTo(401)
        JSONValidator.validateError(result.text,
                expectedErrorCode = "github-token-invalid",
                expectedErrorText = "GitHub token not supplied in Authorization header, or GitHub token was invalid")
    }


    @Test
    fun `authentication succeeds with well-formed Auth header`()
    {
        startApp("auth.provider=github")

        // this is a PAT for a test user who only has access to a test org with no repos
        // reversed so GitHub doesn't spot it and invalidate it
        //val token = "fcef1c6821f7561259ce45d4840965642607e5a4".reversed()
        //val token = "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"

        //token with read:user, read:org and user:email perms
        //val token = "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"

        //token 'as instructions'
        //val token = "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"

        val result = HttpClient.post(url, auth = TestTokenHeader(testUserPAT))

        assertSuccessful(result)

        val json = JsonLoader.fromString(result.text)
        assertThat(json["token_type"].textValue()).isEqualTo("bearer")
        assertThat(json["access_token"]).isNotNull
        assertThat(isLong(json["expires_in"].toString())).isTrue()
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
    fun `authentication fails if token does not have email reading scope`()
    {
        startApp("auth.provider=github")

        // this is a PAT for a test user who only has access to a test org with no repos
        // reversed so GitHub doesn't spot it and invalidate it.
        // This token has read:user and read:org perms only
        val tokenWithoutEmailReadingScope = "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"

        val result = HttpClient.post(url, auth = TestTokenHeader(tokenWithoutEmailReadingScope))

        JSONValidator.validateError(result.text,
                expectedErrorCode = "github-token-invalid",
                expectedErrorText = "GitHub token not supplied in Authorization header, or GitHub token was invalid")

    }

    @Test
    fun `authentication fails if token does not have user reading scope`()
    {
        startApp("auth.provider=github")

        // this is a PAT for a test user who only has access to a test org with no repos
        // reversed so GitHub doesn't spot it and invalidate it
        // This token has user:email and read:org perms only
        val tokenWithoutUserReadingScope = "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".reversed()

        val result = HttpClient.post(url, auth = TestTokenHeader(tokenWithoutUserReadingScope))

        JSONValidator.validateError(result.text,
                expectedErrorCode = "github-token-invalid",
                expectedErrorText = "GitHub token not supplied in Authorization header, or GitHub token was invalid")

    }

    @Test
    fun `authentication fails if token does not have org reading scope`()
    {
        startApp("auth.provider=github")

        // this is a PAT for a test user who only has access to a test org with no repos
        // reversed so GitHub doesn't spot it and invalidate it
        // This token has read:user and user:email perms only
        val tokenWithoutOrgReadingScope = "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"

        val result = HttpClient.post(url, auth = TestTokenHeader(tokenWithoutOrgReadingScope))

        JSONValidator.validateError(result.text,
                expectedErrorCode = "github-token-invalid",
                expectedErrorText = "GitHub token not supplied in Authorization header, or GitHub token was invalid")

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
