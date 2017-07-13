package org.vaccineimpact.reporting_api

import org.vaccineimpact.reporting_api.app_start.DefaultHeadersFilter
import spark.Spark
import spark.route.HttpMethod

data class Endpoint(
        override val urlFragment: String,
        override val controllerName: String,
        override val actionName: String,
        override val contentType: String = ContentTypes.any,
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

    override fun additionalSetup(url: String)
    {
        Spark.after(url, contentType, DefaultHeadersFilter(contentType))
    }

}