package org.vaccineimpact.reporting_api

import org.vaccineimpact.reporting_api.app_start.DefaultHeadersFilter
import org.vaccineimpact.reporting_api.security.MontaguAuthorizer
import org.vaccineimpact.reporting_api.security.PermissionRequirement
import org.vaccineimpact.reporting_api.security.TokenVerifier
import org.vaccineimpact.reporting_api.security.TokenVerifyingConfigFactory
import spark.Spark
import spark.route.HttpMethod

data class JsonEndpoint (
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
        val allPermissions = setOf("*/can-login").map {
            PermissionRequirement.parse(it)
        }
        val configFactory = TokenVerifyingConfigFactory(TokenVerifier.authTokenVerifier, allPermissions.toSet())
        val tokenVerifier = configFactory.build()
        Spark.before(url, org.pac4j.sparkjava.SecurityFilter(
                tokenVerifier,
                configFactory.allClients(),
                MontaguAuthorizer::class.java.simpleName,
                "SkipOptions"
        ))

        Spark.after(url, contentType, DefaultHeadersFilter("${ContentTypes.json}; charset=utf-8"))
    }


    fun transform(x: Any) = Serializer.instance.toResult(x)

}