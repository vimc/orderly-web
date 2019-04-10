package org.vaccineimpact.orderlyweb.app_start

import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.addDefaultResponseHeaders
import org.vaccineimpact.orderlyweb.errors.OrderlyWebError
import spark.Response

class APIErrorHandler
{
    fun handleError(error: OrderlyWebError, res: Response)
    {
        res.body(Serializer.instance.toJson(error.asResult()))
        res.status(error.httpStatus)
        addDefaultResponseHeaders(res.raw(), "${ContentTypes.json}; charset=utf-8")
    }
}