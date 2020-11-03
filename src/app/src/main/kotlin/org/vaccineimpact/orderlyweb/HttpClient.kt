package org.vaccineimpact.orderlyweb

import khttp.responses.Response

interface HttpClient
{
    fun post(url: String, headers: Map<String, String>, json: Map<String, String> = mapOf()): Response
    fun get(url: String, headers: Map<String, String>): Response
    fun delete(url: String, headers: Map<String, String>): Response
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

    override fun delete(url: String, headers: Map<String, String>): Response
    {
        return khttp.delete(url, headers)
    }
}
