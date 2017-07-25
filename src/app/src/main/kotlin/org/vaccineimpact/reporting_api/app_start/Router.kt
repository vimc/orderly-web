package org.vaccineimpact.reporting_api.app_start

import org.slf4j.LoggerFactory
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.DirectActionContext
import org.vaccineimpact.reporting_api.EndpointDefinition
import org.vaccineimpact.reporting_api.JsonEndpoint
import org.vaccineimpact.reporting_api.controllers.Controller
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.errors.UnsupportedValueException
import org.vaccineimpact.reporting_api.security.*
import spark.Route
import spark.Spark
import spark.route.HttpMethod

class Router(val config: RouteConfig)
{

    private val logger = LoggerFactory.getLogger(Router::class.java)

    fun mapEndpoints(urlBase: String): List<String>
    {
        return config.endpoints.map {
            when (it)
            {
                is JsonEndpoint -> mapTransformedEndpoint(it, urlBase)
                else -> mapEndpoint(it, urlBase)
            }
        }
    }

    private fun mapTransformedEndpoint(
            endpoint: JsonEndpoint,
            urlBase: String): String
    {

        val transformer = endpoint::transform
        val fullUrl = urlBase + endpoint.urlFragment
        val route = getWrappedRoute(endpoint)::handle
        val contentType = endpoint.contentType

        logger.info("Mapping $fullUrl to ${endpoint.actionName} on Controller ${endpoint.controllerName}")
        when (endpoint.method)
        {
            HttpMethod.get -> Spark.get(fullUrl, contentType, route, transformer)
            HttpMethod.post -> Spark.post(fullUrl, contentType, route, transformer)
            HttpMethod.put -> Spark.put(fullUrl, contentType, route, transformer)
            HttpMethod.patch -> Spark.patch(fullUrl, contentType, route, transformer)
            HttpMethod.delete -> Spark.delete(fullUrl, contentType, route, transformer)
            else -> throw UnsupportedValueException(endpoint.method)
        }

        endpoint.additionalSetup(fullUrl)
        return fullUrl
    }

    private fun mapEndpoint(
            endpoint: EndpointDefinition,
            urlBase: String): String
    {

        val fullUrl = urlBase + endpoint.urlFragment
        val route = getWrappedRoute(endpoint)::handle
        val contentType = endpoint.contentType

        logger.info("Mapping $fullUrl to ${endpoint.actionName} on Controller ${endpoint.controllerName}")
        when (endpoint.method)
        {
            HttpMethod.get -> Spark.get(fullUrl, contentType, route)
            HttpMethod.post -> Spark.post(fullUrl, contentType, route)
            HttpMethod.put -> Spark.put(fullUrl, contentType, route)
            HttpMethod.patch -> Spark.patch(fullUrl, contentType, route)
            HttpMethod.delete -> Spark.delete(fullUrl, contentType, route)
            else -> throw UnsupportedValueException(endpoint.method)
        }

        endpoint.additionalSetup(fullUrl)
        return fullUrl
    }


    private fun getWrappedRoute(endpoint: EndpointDefinition): Route
    {

        val controllerName = endpoint.controllerName
        val actionName = endpoint.actionName

        val controllerType = Class.forName("org.vaccineimpact.reporting_api.controllers.${controllerName}Controller")

        val controller = controllerType.getConstructor().newInstance() as Controller
        val action = controllerType.getMethod(actionName, ActionContext::class.java)

        return Route({ req, res -> action.invoke(controller, DirectActionContext(req, res)) })
    }


}
