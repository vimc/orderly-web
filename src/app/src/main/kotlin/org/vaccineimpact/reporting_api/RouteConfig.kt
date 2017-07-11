package org.vaccineimpact.reporting_api

import org.slf4j.LoggerFactory
import org.vaccineimpact.api.app.DirectActionContext
import org.vaccineimpact.reporting_api.controllers.*
import org.vaccineimpact.reporting_api.errors.UnsupportedValueException
import spark.Route
import spark.Spark
import spark.route.HttpMethod
import java.util.*

object RouteConfig {

    val logger = LoggerFactory.getLogger(RouteConfig::class.java)

    val endpoints = listOf(

            Endpoint("/reports/", "Report", "getAll"),
            Endpoint("/reports/:name/", "Report", "getByName"),
            Endpoint("/reports/:name/:version/", "Report", "getByNameAndVersion")
    )

    private var controllers: MutableMap<String, Controller> = mutableMapOf()

    fun mapEndpoints(urlBase: String): List<String> {
        return endpoints.map { mapEndpoint(it, urlBase) }
    }

    private fun mapEndpoint(
            endpoint: EndpointDefinition,
            urlBase: String): String {

        val transformer = endpoint::transform
        val fullUrl = urlBase + endpoint.urlFragment
        val route = getWrappedRoute(endpoint)::handle
        val contentType = endpoint.contentType

        logger.info("Mapping $fullUrl")
        when (endpoint.method) {
            HttpMethod.get -> Spark.get(fullUrl, contentType, route, transformer)
            HttpMethod.post -> Spark.post(fullUrl, contentType, route, transformer)
            HttpMethod.put -> Spark.put(fullUrl, contentType, route, transformer)
            HttpMethod.patch -> Spark.patch(fullUrl, contentType, route, transformer)
            HttpMethod.delete -> Spark.delete(fullUrl, contentType, route, transformer)
            else -> throw UnsupportedValueException(endpoint.method)
        }
        return fullUrl
    }

    private fun getWrappedRoute(endpoint: EndpointDefinition): Route {

        val controllerName = endpoint.controllerName
        val actionName = endpoint.actionName

        val controllerType = Class.forName("org.vaccineimpact.reporting_api.controllers.${controllerName}Controller")

        if (!controllers.containsKey(controllerName)) {
            controllers[controllerName] = controllerType.getConstructor().newInstance() as Controller
        }

        val action = controllerType.getMethod(actionName, ActionContext::class.java)

        return Route({ req, res -> action.invoke(controllers[controllerName], DirectActionContext(req, res)) })
    }
}
