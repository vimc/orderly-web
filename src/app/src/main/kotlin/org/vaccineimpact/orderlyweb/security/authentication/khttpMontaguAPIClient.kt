package org.vaccineimpact.orderlyweb.security.authentication

import com.fasterxml.jackson.core.JsonParseException
import com.github.salomonbrys.kotson.fromJson
import org.vaccineimpact.orderlyweb.Serializer
import java.io.IOException

interface MontaguAPIClient
{
    fun getUserDetails(token: String): MontaguUserDetails

    data class MontaguUserDetails(val email: String, val username: String, val displayName: String?)

    // The following are identical to orderlyweb.models.Result, orderlyweb.models.ResultStatus,
    // and orderlyweb.models.ErrorInfo but in principle they need not be and should either spec
    // change could diverge, so defining Montagu specific models here
    class MontaguResult(val status: MontaguResultStatus, data: Any?, errors: Iterable<MontaguErrorInfo>)
    {
        val data = data ?: ""
        val errors = errors.toList()
    }

    enum class MontaguResultStatus
    {
        SUCCESS, FAILURE
    }

    data class MontaguErrorInfo(val code: String, val message: String)
    {
        override fun toString(): String = message
    }

}


class khttpMontaguAPIClient : MontaguAPIClient
{
    override fun getUserDetails(token: String): MontaguAPIClient.MontaguUserDetails
    {
        val response = khttp.get("http://localhost:8080/v1/user/modelling-groups/",
                headers = mapOf("Authorization" to "Bearer $token"))

        val result = parseResult(response.text)

        if (response.statusCode != 200)
        {
            throw MontaguAPIException("Response had errors ${result.errors.joinToString(",") { it.toString() }}", response.statusCode)
        }

        // TODO get real user details once endpoint exists
        return MontaguAPIClient.MontaguUserDetails("test.user@example.com",
                "test.user",
                "Test user")
    }

    private fun parseResult(jsonAsString: String): MontaguAPIClient.MontaguResult
    {
        return try
        {
            Serializer.instance.gson.fromJson(jsonAsString)
        }
        catch (e: JsonParseException)
        {
            throw MontaguAPIException("Failed to parse text as JSON.\nText was: $jsonAsString\n\n$e", 500)
        }
    }
}


class MontaguAPIException(override val message: String, val status: Int) : IOException(message)