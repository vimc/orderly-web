package org.vaccineimpact.reporting_api

import org.vaccineimpact.reporting_api.app_start.DefaultHeadersFilter
import org.vaccineimpact.reporting_api.controllers.verifyToken
import org.vaccineimpact.reporting_api.errors.InvalidOneTimeLinkToken
import org.vaccineimpact.reporting_api.security.MontaguAuthorizer
import spark.Filter
import spark.Request
import spark.Response
import spark.Spark
import spark.route.HttpMethod

data class OnetimeTokenEndpoint(
        override val urlFragment: String,
        override val controllerName: String,
        override val actionName: String,
        override val contentType: String = ContentTypes.binarydata,
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
        Spark.before(url, OneTimeTokenFilter())
    }

}

class OneTimeTokenFilter : Filter{
    override fun handle(request: Request, response: Response) {
        val token = request.queryParams("access_token")
                ?: throw InvalidOneTimeLinkToken("verification", "Access token is missing")

        val claims = verifyToken(token)
    }
}