package org.vaccineimpact.reporting_api

import khttp.responses.Response
import org.vaccineimpact.reporting_api.db.Config

interface OrderlyServerAPI
{
    fun post(url: String, context: ActionContext): Response
    fun get(url: String, context: ActionContext): Response
}

class OrderlyServer(config: Config, private val httpClient: HttpClient) : OrderlyServerAPI
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

        return httpClient.post(fullUrl, standardHeaders, postData)
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
        return if (json.any())
        {
            khttp.post(url, headers, json = json)
        }
        else
        {
            khttp.post(url, headers)
        }
    }

    override fun get(url: String, headers: Map<String, String>): Response
    {
        return khttp.get(url, headers)
    }
}