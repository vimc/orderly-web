package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.errors.OrderlyWebError
import org.vaccineimpact.orderlyweb.models.ErrorInfo

data class ErrorViewModel(val appViewModel: AppViewModel) : AppViewModel by appViewModel
{
    constructor(pageName: String, context: ActionContext) :
            this(DefaultViewModel(context, IndexViewModel.breadcrumb, Breadcrumb(pageName, null)))
}

data class ServerErrorViewModel(val errors: List<ErrorInfo>,
                                val appViewModel: AppViewModel) :
        AppViewModel by appViewModel
{
    constructor(error: OrderlyWebError, context: ActionContext) :
            this(error.problems.toList(), ErrorViewModel("Something went wrong", context))
}
