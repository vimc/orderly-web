package org.vaccineimpact.orderlyweb.app_start

import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.controllers.web.Template
import spark.ModelAndView
import spark.TemplateEngine
import java.lang.reflect.InvocationTargetException

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
            logger.warn(
                    "Exception was thrown whilst using reflection to invoke " +
                    "$controllerType.$actionName, see below for details"
            )
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
            throw NoSuchMethodException(
                    "There is a problem with $controllerType. " +
                            "All controllers must have a constructor with a single parameter of " +
                            "type ActionContext"
            )
        }
        return constructor.newInstance(context) as Controller
    }
}
