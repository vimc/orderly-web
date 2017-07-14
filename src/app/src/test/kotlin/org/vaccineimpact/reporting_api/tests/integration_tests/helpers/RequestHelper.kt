package org.vaccineimpact.reporting_api.tests.integration_tests.helpers

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import khttp.responses.Response
import org.vaccineimpact.reporting_api.ContentTypes

class RequestHelper
{
    init
    {
        CertificateHelper.disableCertificateValidation()
    }

    fun get(url: String, contentType: String = ContentTypes.json): Response
    {
        val headers = mapOf(
                "Accept" to contentType,
                "Accept-Encoding" to "gzip"
        )

        return get("http://localhost:8080/v1" + url, headers)
    }

    private fun get(url: String, headers: Map<String, String>)
            = khttp.get(url, headers)
}

fun <T> Response.montaguData() : T?
{
    val data = this.json()["data"]
    if (data != "")
    {
        @Suppress("UNCHECKED_CAST")
        return data as T
    }
    else
    {
        return null
    }
}

fun Response.json() = Parser().parse(StringBuilder(text)) as JsonObject