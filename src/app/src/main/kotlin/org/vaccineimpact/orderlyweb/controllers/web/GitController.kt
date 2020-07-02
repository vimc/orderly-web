package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.getFriendlyRelativeDateTime
import org.vaccineimpact.orderlyweb.models.GitCommit
import java.text.SimpleDateFormat

class GitController(context: ActionContext): Controller(context)
{
    fun getCommits() : List<GitCommit>
    {
        //TODO: Get from Orderly server when supported, order by age ascending
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return listOf(
               GitCommit("d41cc19", "2020-06-10 10:12:04",
                            getFriendlyRelativeDateTime(formatter.parse("2020-06-10 10:12:04"))),
               GitCommit("e54b124", "2020-06-09 12:43:19",
                            getFriendlyRelativeDateTime(formatter.parse("2020-06-09 12:43:19")))
        )
    }
}