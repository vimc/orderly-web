package org.vaccineimpact.orderlyweb

import spark.Filter
import spark.Request
import spark.Response
import javax.servlet.http.HttpServletResponse

// The idea is that as this file grows, I'll group helpers and split them off into files/classes with more
// specific aims.

fun addDefaultResponseHeaders(req: Request, res: HttpServletResponse,
                              contentType: String = "${ContentTypes.json}; charset=utf-8")
{
    res.contentType = contentType
    val gzip = req.headers("Accept-Encoding")?.contains("gzip")
    if (gzip == true && res.getHeader("Content-Encoding") != "gzip")
    {
        res.addHeader("Content-Encoding", "gzip")
    }
    // This allows cookies to be set and received over AJAX
    res.addHeader("Access-Control-Allow-Credentials", "true")
}

class DefaultHeadersFilter(val contentType: String) : Filter
{
    override fun handle(request: Request, response: Response)
    {
        addDefaultResponseHeaders(request, response.raw(), contentType)
    }
}

fun parseRouteParamToFilepath(routeParam: String): String
{
    return routeParam.replace(":", "/")
}
