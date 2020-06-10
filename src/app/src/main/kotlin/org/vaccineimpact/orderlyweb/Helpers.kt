package org.vaccineimpact.orderlyweb

import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.errors.OrderlyFileNotFoundError
import spark.Filter
import spark.Request
import spark.Response
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.servlet.http.HttpServletResponse
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

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
    return extensionIsOneOf(fileName, arrayOf("png", "jpg", "jpeg", "gif", "svg"))
}

fun extensionIsOneOf(fileName: String, extensions: Array<String>): Boolean
{
    val ext = fileName.toLowerCase().split(".").last()
    return extensions.contains(ext)
}

fun guessFileType(filename: String): String
{
    val ext = File(filename).extension
    return when (ext.toLowerCase())
    {
        "bmp" -> "image/bmp"
        "csv" -> "text/csv"
        "gif" -> "image/gif"
        "jpeg" -> "image/jpg"
        "jpg" -> "image/jpg"
        "png" -> "image/png"
        "svg" -> "image/svg+xml"
        "pdf" -> "application/pdf"
        "htm" -> "text/html"
        "html" -> "text/html"
        "css" -> "text/css"
        else -> ContentTypes.binarydata
    }
}

fun canRenderInBrowser(fileName: String): Boolean
{
    return extensionIsOneOf(fileName, arrayOf("png", "jpg", "jpeg", "gif", "svg", "pdf", "html", "htm", "bmp"))
}

//Mostly stolen from here https://issues.apache.org/jira/browse/IO-373
//Improved version of the same method in commons.io FileUtils, supporting greater precision
//and rounding up as well as down
private enum class SizeSuffix
{
    bytes, KB, MB, GB, TB, PB, EB, ZB, YB
}
fun byteCountToDisplaySize(size: Long, maxChars: Int = 3): String
{
    val KILO_DIVISOR = BigDecimal(1024L)

    var displaySize: String
    var bdSize = BigDecimal(size)
    var selectedSuffix = SizeSuffix.bytes
    for (sizeSuffix in SizeSuffix.values())
    {
        if (sizeSuffix.equals(SizeSuffix.bytes))
        {
            continue
        }
        if (bdSize.setScale(0, RoundingMode.HALF_UP).toString().length <= maxChars)
        {
            break
        }
        selectedSuffix = sizeSuffix
        bdSize = bdSize.divide(KILO_DIVISOR)
    }
    displaySize = bdSize.setScale(0, RoundingMode.HALF_UP).toString()
    if (displaySize.length < maxChars - 1)
    {
        displaySize = bdSize.setScale(
                maxChars - 1 - displaySize.length, RoundingMode.HALF_UP).toString()
    }
    return displaySize + " " + selectedSuffix.toString()
}

fun Controller.downloadFile(files: FileSystem,
                            absoluteFilePath: String,
                            filename: String,
                            contentType: String): Boolean
{
    if (!files.fileExists(absoluteFilePath))
        throw OrderlyFileNotFoundError(filename)

    val response = context.getSparkResponse().raw()

    context.addResponseHeader("Content-Disposition", "attachment; filename=$filename")
    context.addDefaultResponseHeaders(contentType)

    files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

    return true
}

fun getDateStringFromVersionId(id: String): LocalDateTime
{
    val regex = Regex("(\\d{4})(\\d{2})(\\d{2})-(\\d{2})(\\d{2})(\\d{2})-([0-9a-f]{8})")
    val match = regex.matchEntire(id)
            ?.groupValues ?: throw Exception("Badly formatted report id $id")

    return LocalDateTime.parse("${match[1]}-${match[2]}-${match[3]}T${match[4]}:${match[5]}:${match[6]}")
}

private val formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy, HH:mm")
fun getFriendlyDateTime(date: LocalDateTime) : String
{
    return formatter.format(date)
}

private val prettyTime = PrettyTime()
fun getFriendlyRelativeDateTime(date: Date) : String
{
    return prettyTime.format(date)
}