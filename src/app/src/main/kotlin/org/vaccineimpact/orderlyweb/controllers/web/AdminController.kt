package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.models.User

class AdminController(context: ActionContext) : Controller(context)
{

    @Template("admin.ftl")
    fun get(): AdminViewModel
    {
        return AdminViewModel(Serializer.instance.gson.toJson(
                listOf("test.user", "alex.hill").map { User(it, "$it@gmail.com") }))
    }

    fun post(): Boolean
    {
        return true
    }


    data class AdminViewModel(val usersJsonArray: String)

}