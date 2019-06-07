package org.vaccineimpact.orderlyweb.app_start

import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.addDefaultResponseHeaders
import org.vaccineimpact.orderlyweb.errors.OrderlyWebError
import spark.Request
import spark.Response

class APIResponseErrorHandler: ResponseErrorHandler
{
    override fun handleError(error: OrderlyWebError, req: Request, res: Response)
    {
        res.body(Serializer.instance.toJson(error.asResult()))
        res.status(error.httpStatus)
        addDefaultResponseHeaders(res.raw(), "${ContentTypes.json}; charset=utf-8")
    }
}