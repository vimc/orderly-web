package org.vaccineimpact.orderlyweb.app_start

import freemarker.template.Configuration
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.controllers.api.Template
import org.vaccineimpact.orderlyweb.errors.RouteNotFound
import org.vaccineimpact.orderlyweb.errors.UnsupportedValueException
import org.vaccineimpact.orderlyweb.models.AuthenticationResponse
import org.vaccineimpact.orderlyweb.viewmodels.AppViewModel
import spark.ModelAndView
import spark.Route
import spark.Spark
import spark.Spark.notFound
import spark.route.HttpMethod
import spark.template.freemarker.FreeMarkerEngine
import java.lang.reflect.InvocationTargetException
import org.pac4j.sparkjava.CallbackRoute

import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.pac4j.core.config.Config
import org.pac4j.sparkjava.LogoutRoute
import org.vaccineimpact.orderlyweb.security.WebSecurityConfigFactory
import org.vaccineimpact.orderlyweb.security.clients.MontaguIndirectClient


class Router(freeMarkerConfig: Configuration)
{
    private val logger = LoggerFactory.getLogger(Router::class.java)
    private val freeMarkerEngine = FreeMarkerEngine(freeMarkerConfig)

    companion object
    {
        val urls: MutableList<String> = mutableListOf()
        val apiUrlBase = "/api/v1"
    }

    init
    {
        ErrorHandler.setup(freeMarkerEngine)
        mapNotFound()

        val client = AuthenticationConfig.getAuthenticationIndirectClient()
        val config = WebSecurityConfigFactory(client, setOf())
                .build()

        mapLoginCallback(config)
        mapLogoutCallback(config)
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

    private fun mapLoginCallback(config: Config)
    {
        val loginCallback = CallbackRoute(config)
        val url = "login"
        Spark.get(url, loginCallback)
        Spark.get("$url/", loginCallback)
        Spark.get("/$url", loginCallback)
        Spark.get("/$url/", loginCallback)

    }

    private fun mapLogoutCallback(config: Config)
    {
        val logoutCallback = LogoutRoute(config)
        logoutCallback.destroySession = true
        logoutCallback.defaultUrl = "/"
        val url = "logout"
        Spark.get(url, logoutCallback)
        Spark.get("$url/", logoutCallback)
    }

    private fun mapNotFound()
    {
        notFound { req, res ->
            if (req.pathInfo().startsWith(apiUrlBase))
            {
                res.type("${ContentTypes.json}; charset=utf-8")
                Serializer.instance.toJson(RouteNotFound().asResult())
            }
            else
            {
                val context = DirectActionContext(req, res)
                res.type("text/html")
                freeMarkerEngine.render(
                        ModelAndView(AppViewModel(context), "404.ftl")
                )
            }
        }
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

        val template = (action.annotations.firstOrNull { it is Template } as Template?)
        val templateName = template?.templateName

        return try
        {
            val model = action.invoke(controller)
            if (templateName != null)
            {
                freeMarkerEngine.render(
                        ModelAndView(model, templateName)
                )
            }
            else
            {
                model
            }
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