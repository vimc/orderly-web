package org.vaccineimpact.reporting_api

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

    var allowParameterAuthentication = false
    var requireAuthentication = false

    override fun additionalSetup(url: String)
    {
        if (requireAuthentication)
        {
            addSecurityFilter(url)
        }
    }

    private fun addSecurityFilter(url: String)
    {
        val allPermissions = setOf("*/reports.read").map {
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

fun Endpoint.secure(): Endpoint
{
    this.requireAuthentication = true
    return this
}