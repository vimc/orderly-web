package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.getFriendlyDateTime
import org.vaccineimpact.orderlyweb.models.GitCommit
import java.time.LocalDateTime

class GitController(context: ActionContext): Controller(context)
{
    fun getCommits() : List<GitCommit>
    {
        //TODO: Get from Orderly server when supported
        return listOf(
               GitCommit("d41cc19", "2020-06-10 10:12:04",
                            getFriendlyDateTime(LocalDateTime.parse("2020-06-10 10:12:04"))),
               GitCommit("e54b124", "2020-06-09 12:43:19",
                            getFriendlyDateTime(LocalDateTime.parse("2020-06-09 12:43:19")))
        )
    }
}