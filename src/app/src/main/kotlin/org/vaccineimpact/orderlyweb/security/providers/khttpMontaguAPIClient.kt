package org.vaccineimpact.orderlyweb.security.providers

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonSyntaxException
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.db.AppConfig
import java.io.IOException

interface MontaguAPIClient
{
    fun getUserDetails(token: String): UserDetails

    data class UserDetails(val email: String, val username: String, val name: String?)

    // The following are identical to orderlyweb.models.Result, orderlyweb.models.ResultStatus,
    // and orderlyweb.models.ErrorInfo but in principle they need not be and should either spec
    // change could diverge, so defining Montagu specific models here
    data class Result(val status: String, val data: Any?, val errors: List<ErrorInfo>)

    data class ErrorInfo(val code: String, val message: String)
    {
        override fun toString(): String = message
    }

}

class khttpMontaguAPIClient : MontaguAPIClient
{
    private val urlBase = AppConfig()["montagu.api_url"]
    private val serializer = Serializer.instance.gson

    override fun getUserDetails(token: String): MontaguAPIClient.UserDetails
    {
        val response = khttp.get("$urlBase/user/",
                headers = mapOf("Authorization" to "Bearer $token"))

        val result = parseResult(response.text)

        if (response.statusCode != 200)
        {
            throw MontaguAPIException("Response had errors ${result.errors.joinToString(",") { it.toString() }}", response.statusCode)
        }

        return result.data as MontaguAPIClient.UserDetails
    }

    private fun parseResult(jsonAsString: String): MontaguAPIClient.Result
    {
        return try
        {
            val result = serializer.fromJson<MontaguAPIClient.Result>(jsonAsString)
            if (result.data.toString().isNotEmpty())
            {
                result.copy(data = serializer.fromJson<MontaguAPIClient.UserDetails>(serializer.toJson(result.data)))
            }
            else result
        }
        catch (e: JsonSyntaxException)
        {
            throw MontaguAPIException("Failed to parse text as JSON.\nText was: $jsonAsString\n\n$e", 500)
        }

    }
}

class MontaguAPIException(override val message: String, val status: Int) : IOException(message)
