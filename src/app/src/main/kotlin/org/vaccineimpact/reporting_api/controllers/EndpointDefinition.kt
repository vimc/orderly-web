package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.api.app.DirectActionContext
import org.vaccineimpact.reporting_api.ActionContext
import spark.Route
import spark.route.HttpMethod

interface EndpointDefinition
{
    val urlFragment: String
    val route: (ActionContext) -> Any
    val method: HttpMethod
    val contentType: String

    fun additionalSetup(url: String)
    fun transform(x: Any): String
}

fun EndpointDefinition.getWrappedRoute(): Route
{
    return Route({ req, res -> this.route(DirectActionContext(req, res)) })
}