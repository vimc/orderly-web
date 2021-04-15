package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.viewmodels.WorkflowRunViewModel

class WorkflowRunController(context: ActionContext, val orderly: OrderlyClient) : Controller(context)
{
    constructor(context: ActionContext) : this(context, Orderly(context))

    @Template("run-workflow-page.ftl")
    fun getRunWorkflow(): WorkflowRunViewModel
    {
        return WorkflowRunViewModel(context)
    }

    fun getRunWorkflowDetails() : List<WorkflowRun>
    {
        return WorkflowRunRepository.getWorkflowDetails()
    }
}
