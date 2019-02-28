package org.vaccineimpact.orderlyweb.appstart

import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.DirectActionContext
import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.controllers.web.Template
import org.vaccineimpact.orderlyweb.errors.UnsupportedValueException
import spark.ModelAndView
import spark.Route
import spark.Spark
import spark.route.HttpMethod
import spark.template.freemarker.FreeMarkerEngine
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.full.declaredMemberProperties

class Router(val config: RouteConfig) {
    private val logger = LoggerFactory.getLogger(Router::class.java)

    companion object {
        val urls: MutableList<String> = mutableListOf()
    }

    private fun transform(x: Any) = Serializer.instance.toResult(x)

    fun mapEndpoints(urlBase: String) {
        urls.addAll(config.endpoints.map { mapEndpoint(it, urlBase) })
    }

    private fun mapEndpoint(endpoint: EndpointDefinition, urlBase: String): String {
        if (!endpoint.urlFragment.endsWith("/")) {
            throw Exception("All endpoints must end in a forward slash. Problematic endpoint: ${endpoint.urlFragment}")
        }

        val fullUrl = urlBase + endpoint.urlFragment
        logger.info("Mapping $fullUrl to ${endpoint.actionName} on ${endpoint.controller.simpleName}")
        mapUrl(fullUrl, endpoint)
        mapUrl(fullUrl.dropLast(1), endpoint)
        return fullUrl
    }

    private fun mapUrl(fullUrl: String, endpoint: EndpointDefinition) {
        val route = getWrappedRoute(endpoint)::handle
        val contentType = endpoint.contentType
        when (endpoint.transform) {
            true -> when (endpoint.method) {
                HttpMethod.get -> Spark.get(fullUrl, contentType, route, this::transform)
                HttpMethod.post -> Spark.post(fullUrl, contentType, route, this::transform)
                HttpMethod.put -> Spark.put(fullUrl, contentType, route, this::transform)
                HttpMethod.patch -> Spark.patch(fullUrl, contentType, route, this::transform)
                HttpMethod.delete -> Spark.delete(fullUrl, contentType, route, this::transform)
                else -> throw UnsupportedValueException(endpoint.method)
            }
            false -> when (endpoint.method) {
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

    private fun getWrappedRoute(endpoint: EndpointDefinition): Route {
        return Route { req, res -> invokeControllerAction(endpoint, DirectActionContext(req, res)) }
    }

    private fun readProperty(instance: Any, propertyName: String): Any? {
        val clazz = instance.javaClass.kotlin
        return clazz.declaredMemberProperties.first { it.name == propertyName }.get(instance)
    }

    private fun invokeControllerAction(endpoint: EndpointDefinition, context: ActionContext): Any? {
        val controllerType = endpoint.controller.java
        val actionName = endpoint.actionName

        val controller = instantiateController(controllerType, context)
        val action = controllerType.getMethod(actionName)

        val template = (action.annotations.firstOrNull { it is Template } as Template?)
        val templateName = template?.templateName

        return try {
            if (templateName != null) {
                val vm = action.invoke(controller)
                val map = action.returnType.kotlin.declaredMemberProperties.associate {
                    it.name to readProperty(vm, it.name)
                }
                return FreeMarkerEngine().render(
                        ModelAndView(map, "index.ftl")
                )
            }
            action.invoke(controller)
        } catch (e: InvocationTargetException) {
            logger.warn("Exception was thrown whilst using reflection to invoke " +
                    "$controllerType.$actionName, see below for details")
            throw e.targetException
        }
    }

    private fun instantiateController(controllerType: Class<*>, context: ActionContext): Controller {
        val constructor = try {
            controllerType.getConstructor(ActionContext::class.java)
        } catch (e: NoSuchMethodException) {
            throw NoSuchMethodException("There is a problem with $controllerType. " +
                    "All controllers must have a constructor with a single parameter of " +
                    "type ActionContext")
        }
        return constructor.newInstance(context) as Controller
    }

}