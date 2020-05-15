package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller

class GitController(context: ActionContext): Controller(context)
{
    fun getCommits() : List<String>
    {
        //todo: return list of commit objs
        return listOf()
    }
}