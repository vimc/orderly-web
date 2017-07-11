package org.vaccineimpact.reporting_api.controllers

import spark.route.HttpMethod

interface EndpointDefinition
{
    val urlFragment: String
    val controllerName: String
    val actionName: String
    val method: HttpMethod
    val contentType: String

    fun additionalSetup(url: String)
    fun transform(x: Any): String
}