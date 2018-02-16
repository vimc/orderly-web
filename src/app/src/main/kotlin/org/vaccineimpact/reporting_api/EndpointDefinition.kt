package org.vaccineimpact.reporting_api

import spark.route.HttpMethod
import kotlin.reflect.KClass

interface EndpointDefinition
{
    val urlFragment: String
    val controller: KClass<*>
    val actionName: String
    val method: HttpMethod
    val contentType: String
    val transform: Boolean
    val requiredPermissions: Set<String>
    val allowParameterAuthentication: Boolean

    fun additionalSetup(url: String)
}