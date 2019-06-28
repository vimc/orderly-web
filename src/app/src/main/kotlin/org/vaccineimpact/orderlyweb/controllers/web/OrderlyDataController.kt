package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

abstract class OrderlyDataController(actionContext: ActionContext,
                                     val orderly: OrderlyClient = Orderly(actionContext)) : Controller(actionContext)
