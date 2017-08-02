package org.vaccineimpact.reporting_api

import spark.Spark
import spark.route.HttpMethod

class JsonEndpoint(
        urlFragment: String,
        controllerName: String,
        actionName: String,
        method: HttpMethod = HttpMethod.get
) : Endpoint(urlFragment, controllerName, actionName, ContentTypes.json, method)
{
    override fun additionalSetup(url: String)
    {
        super.additionalSetup(url)
        Spark.after(url, contentType, DefaultHeadersFilter("${ContentTypes.json}; charset=utf-8"))
    }

    fun transform(x: Any) = Serializer.instance.toResult(x)

}