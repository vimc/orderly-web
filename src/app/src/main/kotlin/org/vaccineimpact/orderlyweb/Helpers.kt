package org.vaccineimpact.orderlyweb

import spark.Filter
import spark.Request
import spark.Response
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import javax.servlet.http.HttpServletResponse

// The idea is that as this file grows, I'll group helpers and split them off into files/classes with more
// specific aims.

fun addDefaultResponseHeaders(res: HttpServletResponse,
                              contentType: String = "${ContentTypes.json}; charset=utf-8")
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
    return URLDecoder.decode(routeParam.replace(":", "/"), "UTF-8") //route param may include URL encoding
}

fun encodeFilename(filename: String): String
{
    return URLEncoder.encode(filename.replace("/", ":"), "UTF-8")
}

fun isImage(fileName: String): Boolean
{
    return extensionIsOneOf(fileName, arrayOf("png", "jpg", "jpeg", "gif", "svg", "pdf"))
}

fun extensionIsOneOf(fileName: String, extensions: Array<String>): Boolean
{
    val ext = fileName.toLowerCase().split(".").last()
    return extensions.contains(ext)
}

fun guessFileType(filename: String): String
{
    val ext = File(filename).extension
    return when (ext)
    {
        "csv" -> "text/csv"
        "png" -> "image/png"
        "svg" -> "image/svg+xml"
        "pdf" -> "application/pdf"
        "html" -> "text/html"
        "css" -> "text/css"
        else -> ContentTypes.binarydata
    }
}

fun canRenderInBrowser(fileName: String): Boolean
{
    return extensionIsOneOf(fileName, arrayOf("png", "jpg", "jpeg", "gif", "svg", "pdf", "html", "htm"))
}
