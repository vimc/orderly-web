package org.vaccineimpact.orderlyweb.security

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

fun String.inflated() = inflate(this)
fun String.deflated() = deflate(this)

private fun deflate(uncompressed: String): String
{
    val bytes = ByteArrayOutputStream().use { byteStream ->
        GZIPOutputStream(byteStream).use {
            it.write(uncompressed.toByteArray())
            it.flush()
        }
        byteStream.toByteArray()
    }
    return Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes)
}

@JvmName("inflateNullable")
fun inflate(compressed: String?): String?
{
    return if (compressed == null)
    {
        null
    }
    else
    {
        inflate(compressed)
    }
}

fun inflate(compressed: String): String
{
    if (compressed.isEmpty())
    {
        return ""
    }

    val bytes = try
    {
        Base64.getUrlDecoder().decode(compressed)
    }
    catch (e: IllegalArgumentException)
    {
        null
    }

    return if (bytes != null && isCompressed(bytes))
    {
        val inputStream = GZIPInputStream(ByteArrayInputStream(bytes))
        InputStreamReader(inputStream, "UTF-8")
                .buffered()
                .readLines()
                .joinToString("\n")
    }
    else
    {
        compressed
    }
}

private fun isCompressed(compressed: ByteArray) =
        compressed[0] == (GZIPInputStream.GZIP_MAGIC.toByte())
                && compressed[1] == (GZIPInputStream.GZIP_MAGIC shr 8).toByte()