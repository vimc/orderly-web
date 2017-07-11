package org.vaccineimpact.reporting_api.controllers

import org.json.JSONObject
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.Orderly
import org.vaccineimpact.reporting_api.OrderlyClient

class ArtefactController(orderlyClient: OrderlyClient? = null)  : Controller
{
    val orderly = orderlyClient?: Orderly()

    fun get(context: ActionContext): JSONObject {
        return orderly.getArtefacts(context.params(":name"), context.params(":version"))
    }

}
