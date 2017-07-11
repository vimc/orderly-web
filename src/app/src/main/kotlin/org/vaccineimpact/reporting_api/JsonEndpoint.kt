package org.vaccineimpact.reporting_api

import org.vaccineimpact.reporting_api.app_start.DefaultHeadersFilter
import spark.Spark
import spark.route.HttpMethod

data class JsonEndpoint (
        override val urlFragment: String,
        override val controllerName: String,
        override val actionName: String,
        override val method: HttpMethod = HttpMethod.get,
        override val contentType: String = ContentTypes.json
) : EndpointDefinition
{
    init
    {
        if (!urlFragment.endsWith("/"))
        {
            throw Exception("All endpoint definitions must end with a forward slash: $urlFragment")
        }
    }

    override fun additionalSetup(url: String)
    {
        Spark.after(url, contentType, DefaultHeadersFilter(contentType))
    }

    fun transform(x: Any) = Serializer.instance.toResult(x)

}