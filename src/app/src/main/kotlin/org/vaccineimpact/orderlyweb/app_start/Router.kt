package org.vaccineimpact.orderlyweb.app_start

import freemarker.template.Configuration
import org.pac4j.sparkjava.CallbackRoute
import org.pac4j.sparkjava.LogoutRoute
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.controllers.web.Template
import org.vaccineimpact.orderlyweb.errors.RouteNotFound
import org.vaccineimpact.orderlyweb.errors.UnsupportedValueException
import org.vaccineimpact.orderlyweb.models.AuthenticationResponse
import org.vaccineimpact.orderlyweb.security.WebSecurityConfigFactory
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationProvider
import spark.ModelAndView
import spark.Route
import spark.Spark
import spark.Spark.notFound
import spark.TemplateEngine
import spark.route.HttpMethod
import spark.template.freemarker.FreeMarkerEngine
import java.lang.reflect.InvocationTargetException


class Router(private val templateEngine: TemplateEngine,
             private val actionResolver: ActionResolver,
             private val authenticationController: AuthenticationRouteBuilder)
{
    constructor(templateEngine: TemplateEngine) :
            this(templateEngine, ActionResolver(templateEngine), AuthenticationRouteBuilder(AuthenticationConfig()))

    constructor(freeMarkerConfig: Configuration) :
            this(FreeMarkerEngine(freeMarkerConfig))

    private val logger = LoggerFactory.getLogger(Router::class.java)

    companion object
    {
        val urls: MutableList<String> = mutableListOf()
        const val apiUrlBase = "/api/v1"
    }

    init
    {
        ErrorHandler.setup(templateEngine)
        mapNotFound()

        mapLoginCallback()
        mapLogoutCallback()
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

    private fun mapLoginCallback()
    {
        val loginCallback = authenticationController.loginRoute()
        val url = "login"
        Spark.get(url, loginCallback)
        Spark.get("$url/", loginCallback)

    }

    private fun mapLogoutCallback()
    {
        val logoutCallback = authenticationController.logoutRoute()
        val url = "logout"
        Spark.get(url, logoutCallback)
        Spark.get("$url/", logoutCallback)
    }

    private fun mapNotFound()
    {
        notFound { _, _ ->
            throw RouteNotFound()
        }
    }

    private fun mapEndpoint(endpoint: EndpointDefinition, urlBase: String): String
    {
        val fullUrl = urlBase + endpoint.urlFragment
        logger.info("Mapping $fullUrl to ${endpoint.actionName} on ${endpoint.controller.simpleName}")
        mapUrl(fullUrl, endpoint)
        if (fullUrl.last() == '/')
        {
            mapUrl(fullUrl.dropLast(1), endpoint)
        }
        else
        {
            mapUrl("$fullUrl/", endpoint)
        }
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
        return Route { req, res -> actionResolver.invokeControllerAction(endpoint, DirectActionContext(req, res)) }
    }
}

class AuthenticationRouteBuilder(private val authenticationConfig: AuthenticationConfig)
{
    private val client = authenticationConfig.getAuthenticationIndirectClient()
    private val securityConfig = WebSecurityConfigFactory(client, setOf())
            .build()

    fun logoutRoute(): LogoutRoute
    {
        val synchroniseLogout = authenticationConfig.getConfiguredProvider() == AuthenticationProvider.Montagu
        return LogoutRoute(securityConfig)
                .apply {
                    destroySession = true
                    defaultUrl = "/"
                    centralLogout = synchroniseLogout
                }
    }

    fun loginRoute(): CallbackRoute
    {
        return CallbackRoute(securityConfig)
    }
}

class ActionResolver(private val templateEngine: TemplateEngine)
{
    private val logger = LoggerFactory.getLogger(ActionResolver::class.java)

    fun invokeControllerAction(endpoint: EndpointDefinition, context: ActionContext): Any?
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
                templateEngine.render(
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
