package org.vaccineimpact.reporting_api

import org.slf4j.LoggerFactory
import org.vaccineimpact.reporting_api.controllers.*
import org.vaccineimpact.reporting_api.errors.UnsupportedValueException
import spark.Spark
import spark.route.HttpMethod

object RouteConfig {

    val logger = LoggerFactory.getLogger(RouteConfig::class.java)

    val reportController = ReportController()

    val endpoints = listOf(

            Endpoint("/reports/", reportController::getAll),
            Endpoint("/reports/:name/", reportController::getByName),
            Endpoint("/reports/:name/:version/", reportController::getByNameAndVersion)
    )

    fun mapEndpoints(urlBase: String): List<String> {
        return endpoints.map { mapEndpoint(it, urlBase) }
    }

    private fun mapEndpoint(
            endpoint: EndpointDefinition,
            urlBase: String): String {
        val transformer = endpoint::transform
        val fullUrl = urlBase + endpoint.urlFragment
        val route = endpoint.getWrappedRoute()::handle
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
}
