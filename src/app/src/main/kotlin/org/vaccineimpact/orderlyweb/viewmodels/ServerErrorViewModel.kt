package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.errors.OrderlyWebError

class ServerErrorViewModel(error: OrderlyWebError,
                           context: ActionContext) :
        AppViewModel by DefaultViewModel(context, IndexViewModel.breadcrumb, Breadcrumb("Something went wrong", null))
{
    val errors = error.problems
}