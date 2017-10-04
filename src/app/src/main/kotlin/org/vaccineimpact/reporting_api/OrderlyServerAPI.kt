package org.vaccineimpact.reporting_api

import org.vaccineimpact.reporting_api.db.Config

interface OrderlyServerAPI
{
    fun post(url: String, postData: Map<String, String>)
}

class OrderlyServer(val config: Config): OrderlyServerAPI
{
    private val urlBase: String = config["orderly.server"]

    override fun post(url: String, postData: Map<String, String>)
    {
        khttp.post(urlBase + url, data = postData)
    }

}