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
               GitCommit("d41cc19", "2020-06-02T08:51:251",
                            getFriendlyDateTime(LocalDateTime.parse("2020-06-02T08:51:21"))),
               GitCommit("e54b124", "2020-06-01T09:15:115",
                            getFriendlyDateTime(LocalDateTime.parse("2020-06-01T09:15:11")))
        )
    }
}