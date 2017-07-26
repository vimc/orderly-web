package org.vaccineimpact.reporting_api

import spark.route.HttpMethod

interface EndpointDefinition {
    val urlFragment: String
    val controllerName: String
    val actionName: String
    val method: HttpMethod
    val contentType: String

    fun additionalSetup(url: String)
}