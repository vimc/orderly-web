package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.viewmodels.WorkflowRunViewModel
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebWorkflowRunRepository
import org.vaccineimpact.orderlyweb.db.repositories.WorkflowRunRepository
import org.vaccineimpact.orderlyweb.models.WorkflowRun

class WorkflowRunController(
    context: ActionContext,
    private val workflowRunRepository: WorkflowRunRepository
) : Controller(context)
{
    constructor(context: ActionContext) : this(context, OrderlyWebWorkflowRunRepository())

    fun getRunWorkflowDetails(): WorkflowRun
    {
        val key = context.params(":key")
        return workflowRunRepository.getWorkflowDetails(key)
    }

    @Template("run-workflow-page.ftl")
    fun getRunWorkflow(): WorkflowRunViewModel
    {
        return WorkflowRunViewModel(context)
    }
}
