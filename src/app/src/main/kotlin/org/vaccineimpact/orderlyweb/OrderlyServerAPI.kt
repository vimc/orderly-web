package org.vaccineimpact.orderlyweb

import khttp.responses.Response
import org.vaccineimpact.orderlyweb.db.Config

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
        val queryPart = if (queryString != null)
        {
            "?" + queryString
        }
        else
        {
            ""
        }
        return urlBase + url + queryPart
    }
}