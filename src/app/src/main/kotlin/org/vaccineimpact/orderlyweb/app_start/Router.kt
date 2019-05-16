package org.vaccineimpact.orderlyweb.app_start

import freemarker.template.Configuration
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.errors.RouteNotFound
import org.vaccineimpact.orderlyweb.models.AuthenticationResponse
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import spark.ResponseTransformer
import spark.Route
import spark.Spark.notFound
import spark.TemplateEngine
import spark.template.freemarker.FreeMarkerEngine


class Router(private val actionResolver: ActionResolver,
             private val authenticationRouteBuilder: AuthenticationRouteBuilder,
             private val sparkWrapper: SparkWrapper,
             private val errorHandler: ErrorHandler)
{
    constructor(templateEngine: TemplateEngine) :
            this(ActionResolver(templateEngine),
                    OrderlyAuthenticationRouteBuilder(AuthenticationConfig()),
                    SparkServiceWrapper(),
                    ErrorHandler(templateEngine))

    constructor(freeMarkerConfig: Configuration) :
            this(FreeMarkerEngine(freeMarkerConfig))

    private val logger = LoggerFactory.getLogger(Router::class.java)
    private val responseTransformer = OrderlyWebResponseTransformer()

    companion object
    {
        val urls: MutableList<String> = mutableListOf()
        const val apiUrlBase = "/api/v1"
    }

    init
    {
        mapNotFound()
        mapLoginCallback()
        mapLogoutCallback()
    }

    fun mapEndpoints(routeConfig: RouteConfig, urlBase: String)
    {
        urls.addAll(routeConfig.endpoints.map { mapEndpoint(it, urlBase) })
    }

    private fun mapLoginCallback()
    {
        val loginCallback = authenticationRouteBuilder.loginCallback()
        val url = "login"
        sparkWrapper.mapGet(url, loginCallback)
        sparkWrapper.mapGet("$url/", loginCallback)
    }

    private fun mapLogoutCallback()
    {
        val logoutCallback = authenticationRouteBuilder.logout()
        val url = "logout"
        sparkWrapper.mapGet(url, logoutCallback)
        sparkWrapper.mapGet("$url/", logoutCallback)
    }

    private fun mapNotFound()
    {
        notFound { req, res ->
            errorHandler.handleError(RouteNotFound(), req, res)
        }
    }

    private fun mapEndpoint(endpoint: EndpointDefinition, urlBase: String): String
    {
        var fullUrl = urlBase + endpoint.urlFragment

        if (fullUrl.last() != '/')
        {
            fullUrl = "$fullUrl/"
        }

        logger.info("Mapping $fullUrl to ${endpoint.actionName} on ${endpoint.controller.simpleName}")

        mapUrl(fullUrl, endpoint)
        mapUrl(fullUrl.dropLast(1), endpoint)
        return fullUrl
    }

    private fun mapUrl(fullUrl: String, endpoint: EndpointDefinition)
    {
        val route = getWrappedRoute(endpoint)
        val contentType = endpoint.contentType
        when (endpoint.transform)
        {
            true -> sparkWrapper.map(fullUrl, endpoint.method, contentType, route, responseTransformer)
            false -> sparkWrapper.map(fullUrl, endpoint.method, contentType, route)
        }

        endpoint.additionalSetup(fullUrl)
    }

    private fun getWrappedRoute(endpoint: EndpointDefinition): Route
    {
        return Route { req, res -> actionResolver.invokeControllerAction(endpoint, DirectActionContext(req, res)) }
    }
}

class OrderlyWebResponseTransformer : ResponseTransformer
{
    override fun render(model: Any) = when (model)
    {
        is AuthenticationResponse -> Serializer.instance.gson.toJson(model)!!
        else -> Serializer.instance.toResult(model)
    }
}