package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.DefaultHeadersFilter
import org.vaccineimpact.reporting_api.Serializer
import spark.Spark
import spark.route.HttpMethod

data class Endpoint (
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

    override fun transform(x: Any) = Serializer.instance.toResult(x)

}