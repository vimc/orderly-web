package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.Report
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class HomeController(context: ActionContext,
                     private val orderly: OrderlyClient) : Controller(context) {

    constructor(context: ActionContext) :
            this(context, Orderly())

    @Template("index.ftl")
    fun get(): HomeViewModel {
        return HomeViewModel(orderly.getAllReports())
    }

}

@Target(AnnotationTarget.FUNCTION)
annotation class Template(val templateName: String)


data class HomeViewModel(val reports: List<Report>) : ViewModel {

    override fun toMap(): Map<String, Any?> {
        return HomeViewModel::class.memberProperties.associate {
            it.name to it.get(this)
        }
    }
}

interface ViewModel {
    fun toMap(): Map<String, Any?>
}

