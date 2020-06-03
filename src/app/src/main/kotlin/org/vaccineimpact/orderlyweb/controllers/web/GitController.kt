package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.models.GitCommit

class GitController(context: ActionContext): Controller(context)
{
    fun getCommits() : List<GitCommit>
    {
        //TODO: Get from Orderly server when supported
        return listOf(
               GitCommit("d41cc19", "2020-06-02T08:51:25Z"),
               GitCommit("e54b124", "2020-06-01T09:15:115Z")
        )
    }
}