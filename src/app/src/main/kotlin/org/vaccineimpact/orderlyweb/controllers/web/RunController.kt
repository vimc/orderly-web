package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.models.GitBranch
import org.vaccineimpact.orderlyweb.models.RunReportMetadata

abstract class RunController(context: ActionContext,  protected val orderlyServerAPI: OrderlyServerAPI) : Controller(context)
{
    
}
