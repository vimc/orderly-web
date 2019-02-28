package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller

class LoginController(context: ActionContext) : Controller(context) {

    @Template("login.ftl")
    fun get() {

    }
    
}