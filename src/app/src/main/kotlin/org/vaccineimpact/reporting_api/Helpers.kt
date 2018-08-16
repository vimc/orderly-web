package org.vaccineimpact.reporting_api

import spark.Filter
import spark.Request
import spark.Response
import javax.servlet.http.HttpServletResponse

// The idea is that as this file grows, I'll group helpers and split them off into files/classes with more
// specific aims.

fun addTrailingSlashes(req: Request, res: Response)
{
    if (!req.pathInfo().endsWith("/"))
    {
        var path = req.pathInfo() + "/"
        if (req.queryString() != null)
        {
            path += "?" + req.queryString()
        }
        res.redirect(path)
    }
}

fun addDefaultResponseHeaders(res: HttpServletResponse, contentType: String)
{
    if (!res.containsHeader("Content-Encoding"))
    {
        res.contentType = contentType
        res.addHeader("Content-Encoding", "gzip")
        // This allows cookies to be received over AJAX
        res.addHeader("Access-Control-Allow-Credentials", "true")
    }
}

class DefaultHeadersFilter(val contentType: String) : Filter
{
    override fun handle(request: Request, response: Response)
    {
        addDefaultResponseHeaders(response.raw(), contentType)
    }
}

fun parseRouteParamToFilepath(routeParam: String): String
{
    return routeParam.replace(":", "/")
}