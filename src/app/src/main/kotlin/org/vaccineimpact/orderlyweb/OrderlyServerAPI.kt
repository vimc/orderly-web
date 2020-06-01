package org.vaccineimpact.orderlyweb

import khttp.responses.Response
import org.json.JSONArray
import org.vaccineimpact.orderlyweb.db.Config

interface OrderlyServerAPI
{
    fun post(url: String, context: ActionContext): OrderlyServerResponse
    fun get(url: String, context: ActionContext): OrderlyServerResponse
}

data class OrderlyServerResponse(val text: String, val statusCode: Int)

class OrderlyServer(config: Config, private val httpClient: HttpClient) : OrderlyServerAPI
{
    private val urlBase: String = "${config["orderly.server"]}/v1"

    private val standardHeaders = mapOf(
            "Accept" to ContentTypes.json,
            "Accept-Encoding" to "gzip"
    )

    override fun post(url: String, context: ActionContext): OrderlyServerResponse
    {
        val fullUrl = buildFullUrl(url, context.queryString())
        val postData = context.postData<String>()
        val response =  httpClient.post(fullUrl, standardHeaders, postData)
        return transformResponse(response)
    }

    override fun get(url: String, context: ActionContext): OrderlyServerResponse
    {
        val fullUrl = buildFullUrl(url, context.queryString())
        val response = httpClient.get(fullUrl, standardHeaders)
        return transformResponse(response)
    }

    private fun transformResponse(rawResponse: Response): OrderlyServerResponse
    {
        val json = rawResponse.jsonObject; //this is a copy, we're not updating response here
        val errors = json["errors"]
        val newErrors = JSONArray() //TODO: add actual errors, having transformed them!!

        json.put("errors", newErrors)

        return OrderlyServerResponse(json.toString(), rawResponse.statusCode)
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