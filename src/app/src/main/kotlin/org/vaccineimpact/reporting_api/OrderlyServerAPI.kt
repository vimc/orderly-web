package org.vaccineimpact.reporting_api

import khttp.responses.Response
import org.json.HTTP
import org.vaccineimpact.reporting_api.db.ConfigWrapper

interface OrderlyServerAPI
{
    fun post(url: String, context: ActionContext): Response
    fun get(url: String, context: ActionContext): Response
}

class OrderlyServer(val config: ConfigWrapper, val httpClient: HttpClient) : OrderlyServerAPI
{
    private val urlBase: String = "${config["orderly.server"]}/v1"

    private val standardHeaders = mapOf(
            "Accept" to ContentTypes.json,
            "Accept-Encoding" to "gzip"
    )

    override fun post(url: String, context: ActionContext): Response
    {
        val fullUrl = buildFullUrl(url, context.queryString())
        val postData = context.postData()

        if (context.postData().any())
            return httpClient.post(fullUrl, standardHeaders, json = postData)

        return httpClient.post(fullUrl, standardHeaders)
    }

    override fun get(url: String, context: ActionContext): Response
    {
        val fullUrl = buildFullUrl(url, context.queryString())
        return httpClient.get(fullUrl, standardHeaders)
    }

    private fun buildFullUrl(url: String, queryString: String?): String
    {
        if (queryString == null)
            return "$urlBase$url"

        return "$urlBase$url?$queryString"
    }
}

interface HttpClient
{
    fun post(url: String, headers: Map<String, String>, json: Map<String, String> = mapOf()): Response
    fun get(url: String, headers: Map<String, String>): Response
}

class KHttpClient : HttpClient
{
    override fun post(url: String, headers: Map<String, String>, json: Map<String, String>): Response
    {
        return khttp.post(url, headers, json = json)
    }

    override fun get(url: String, headers: Map<String, String>): Response
    {
        return khttp.get(url, headers)
    }
}