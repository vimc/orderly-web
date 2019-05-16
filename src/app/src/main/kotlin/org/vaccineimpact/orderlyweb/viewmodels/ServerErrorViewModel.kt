package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.errors.OrderlyWebError

open class ServerErrorViewModel(error: OrderlyWebError,
                                context: ActionContext) :
        AppViewModel(context, IndexViewModel.breadcrumb, Breadcrumb("Something went wrong", null))
{
    open val errors = error.problems
}