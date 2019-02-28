package org.vaccineimpact.orderlyweb

import org.vaccineimpact.orderlyweb.security.PermissionRequirement
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
    val requiredPermissions: List<PermissionRequirement>
    val allowParameterAuthentication: Boolean

    fun additionalSetup(url: String)
}