package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebWorkflowRunRepository
import org.vaccineimpact.orderlyweb.db.repositories.WorkflowRunRepository
import org.vaccineimpact.orderlyweb.models.WorkflowRun

class WorkflowRunController(
        context: ActionContext,
        val orderly: OrderlyClient,
        private val workflowRunRepository: WorkflowRunRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, Orderly(context), OrderlyWebWorkflowRunRepository())

    fun getRunWorkflowDetails() : WorkflowRun
    {
        val key = context.queryParams("key").toString()
        return workflowRunRepository.getWorkflowDetails(key)
    }
}
