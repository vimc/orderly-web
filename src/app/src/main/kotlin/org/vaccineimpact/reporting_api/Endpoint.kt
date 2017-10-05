package org.vaccineimpact.reporting_api

import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.reporting_api.security.*
import spark.Spark
import spark.route.HttpMethod

open class Endpoint(
        override final val urlFragment: String,
        override final val controllerName: String,
        override final val actionName: String,
        override final val contentType: String = ContentTypes.binarydata,
        override final val method: HttpMethod = HttpMethod.get
) : EndpointDefinition
{
    init
    {
        if (!urlFragment.endsWith("/"))
        {
            throw Exception("All endpoint definitions must end with a forward slash: $urlFragment")
        }
    }

    val requiredPermissions = mutableSetOf<String>()

    var allowParameterAuthentication = false
    override var transform = false

    override fun additionalSetup(url: String)
    {
        if (requiredPermissions.any())
        {
            addSecurityFilter(url)
        }
        if (this.contentType == ContentTypes.json)
        {
            Spark.after(url, ContentTypes.json, DefaultHeadersFilter("${ContentTypes.json}; charset=utf-8"))
        }
    }

    override fun transformer(x: Any) = Serializer.instance.toResult(x)

    private fun addSecurityFilter(url: String)
    {
        val allPermissions = this.requiredPermissions.map {
            PermissionRequirement.parse(it)
        }

        var configFactory = TokenVerifyingConfigFactory(
                allPermissions.toSet())

        if (allowParameterAuthentication)
        {
            configFactory = configFactory.allowParameterAuthentication()
        }

        val config = configFactory.build()

        Spark.before(url, org.pac4j.sparkjava.SecurityFilter(
                config,
                configFactory.allClients(),
                MontaguAuthorizer::class.java.simpleName,
                "SkipOptions"
        ))
    }

}

fun Endpoint.allowParameterAuthentication(): Endpoint
{
    this.allowParameterAuthentication = true
    return this
}

fun Endpoint.secure(requiredPermissions: Set<String>): Endpoint
{
    this.requiredPermissions.addAll(requiredPermissions)
    return this
}

fun Endpoint.transform(): Endpoint
{
    this.transform = true
    return this
}