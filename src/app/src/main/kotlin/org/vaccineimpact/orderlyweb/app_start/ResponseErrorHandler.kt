package org.vaccineimpact.orderlyweb.app_start

import org.vaccineimpact.orderlyweb.errors.OrderlyWebError
import spark.Request
import spark.Response

interface ResponseErrorHandler
{
    fun handleError(error: OrderlyWebError, req: Request, res: Response)
}
