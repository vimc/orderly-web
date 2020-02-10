package org.vaccineimpact.orderlyweb.app_start

import freemarker.template.*
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

class TemplateObjectWrapper : freemarker.ext.beans.BeansWrapper(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
{
    private val baseObjectWrapper = DefaultObjectWrapper(this.incompatibleImprovements)

    override fun wrap(model: Any): TemplateModel
    {
        val properties = model::class.memberProperties
        val result = mutableMapOf<String, Any?>()
        properties.map {
            val propertyValue = it.getter.call(model)
            val annotation = it.findAnnotation<Serialise>()
            if (annotation != null)
            {
                result[annotation.propertyName] = Serializer.instance.gson.toJson(propertyValue)
            }
            result[it.name] = propertyValue
        }
        return baseObjectWrapper.wrap(result)
    }

}