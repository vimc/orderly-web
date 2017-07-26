package org.vaccineimpact.reporting_api

import org.vaccineimpact.reporting_api.app_start.DefaultHeadersFilter
import spark.Spark
import spark.route.HttpMethod

data class JsonEndpoint(
        override val urlFragment: String,
        override val controllerName: String,
        override val actionName: String,
        override val method: HttpMethod = HttpMethod.get
) : EndpointDefinition
{
    init
    {
        if (!urlFragment.endsWith("/"))
        {
            throw Exception("All endpoint definitions must end with a forward slash: $urlFragment")
        }
    }

    override val contentType: String = ContentTypes.json

    override fun additionalSetup(url: String)
    {
        Spark.after(url, contentType, DefaultHeadersFilter("${ContentTypes.json}; charset=utf-8"))
    }

    fun transform(x: Any) = Serializer.instance.toResult(x)

}