package org.vaccineimpact.orderlyweb.app_start

import freemarker.template.Configuration
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.controllers.Template
import org.vaccineimpact.orderlyweb.errors.RouteNotFound
import org.vaccineimpact.orderlyweb.errors.UnsupportedValueException
import org.vaccineimpact.orderlyweb.models.AuthenticationResponse
import org.vaccineimpact.orderlyweb.viiewmodels.AppViewModel
import spark.ModelAndView
import spark.Route
import spark.Spark
import spark.Spark.notFound
import spark.route.HttpMethod
import spark.template.freemarker.FreeMarkerEngine
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

class Router(freeMarkerConfig: Configuration)
{
    private val logger = LoggerFactory.getLogger(Router::class.java)
    private val freeMarkerEngine = FreeMarkerEngine(freeMarkerConfig)

    companion object
    {
        val urls: MutableList<String> = mutableListOf()
    }

    private fun transform(x: Any) = when (x)
    {
        is AuthenticationResponse -> Serializer.instance.gson.toJson(x)!!
        else -> Serializer.instance.toResult(x)
    }

    fun mapEndpoints(routeConfig: RouteConfig, urlBase: String)
    {
        urls.addAll(routeConfig.endpoints.map { mapEndpoint(it, urlBase) })
    }

    private fun mapEndpoint(endpoint: EndpointDefinition, urlBase: String): String
    {
        val fullUrl = urlBase + endpoint.urlFragment
        logger.info("Mapping $fullUrl to ${endpoint.actionName} on ${endpoint.controller.simpleName}")
        mapUrl(fullUrl, endpoint)
        mapUrl(fullUrl.dropLast(1), endpoint)
        return fullUrl
    }

    private fun mapUrl(fullUrl: String, endpoint: EndpointDefinition)
    {
        val route = getWrappedRoute(endpoint)::handle
        val contentType = endpoint.contentType
        when (endpoint.transform)
        {
            true -> when (endpoint.method)
            {
                HttpMethod.get -> Spark.get(fullUrl, contentType, route, this::transform)
                HttpMethod.post -> Spark.post(fullUrl, contentType, route, this::transform)
                HttpMethod.put -> Spark.put(fullUrl, contentType, route, this::transform)
                HttpMethod.patch -> Spark.patch(fullUrl, contentType, route, this::transform)
                HttpMethod.delete -> Spark.delete(fullUrl, contentType, route, this::transform)
                else -> throw UnsupportedValueException(endpoint.method)
            }
            false -> when (endpoint.method)
            {
                HttpMethod.get -> Spark.get(fullUrl, contentType, route)
                HttpMethod.post -> Spark.post(fullUrl, contentType, route)
                HttpMethod.put -> Spark.put(fullUrl, contentType, route)
                HttpMethod.patch -> Spark.patch(fullUrl, contentType, route)
                HttpMethod.delete -> Spark.delete(fullUrl, contentType, route)
                else -> throw UnsupportedValueException(endpoint.method)
            }
        }

        notFound { req, res ->
            val acceptHeader = req.headers("Accept")
            if (acceptHeader.contains("text/html") || acceptHeader.contains("*/*")) {
                res.type("text/html")
                freeMarkerEngine.render(
                        ModelAndView(AppViewModel(), "404.ftl")
                )

            } else {
                res.type("${ContentTypes.json}; charset=utf-8")
                Serializer.instance.toJson(RouteNotFound().asResult())
            }
        }

        endpoint.additionalSetup(fullUrl)
    }

    private fun getWrappedRoute(endpoint: EndpointDefinition): Route
    {
        return Route { req, res -> invokeControllerAction(endpoint, DirectActionContext(req, res)) }
    }

    private fun invokeControllerAction(endpoint: EndpointDefinition, context: ActionContext): Any?
    {
        val controllerType = endpoint.controller.java
        val actionName = endpoint.actionName

        val controller = instantiateController(controllerType, context)
        val action = controllerType.getMethod(actionName)

        return try
        {
            action.invoke(controller)
        }
        catch (e: InvocationTargetException)
        {
            logger.warn("Exception was thrown whilst using reflection to invoke " +
                    "$controllerType.$actionName, see below for details")
            throw e.targetException
        }
    }

    private fun instantiateController(controllerType: Class<*>, context: ActionContext): Controller
    {
        val constructor = try
        {
            controllerType.getConstructor(ActionContext::class.java)
        }
        catch (e: NoSuchMethodException)
        {
            throw NoSuchMethodException("There is a problem with $controllerType. " +
                    "All controllers must have a constructor with a single parameter of " +
                    "type ActionContext")
        }
        return constructor.newInstance(context) as Controller
    }

}