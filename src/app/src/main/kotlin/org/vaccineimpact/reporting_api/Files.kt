package org.vaccineimpact.reporting_api

import java.io.*
import java.util.zip.GZIPOutputStream

interface FileSystem
{
    fun writeFileToOutputStream(absoluteFilePath: String, outputStream: OutputStream)
    fun fileExists(absoluteFilePath: String): Boolean
}

class Files : FileSystem
{

    override fun writeFileToOutputStream(absoluteFilePath: String, outputStream: OutputStream)
    {
        val buffer = ByteArray(1024)
        val bufferSize = 8000

        BufferedInputStream(FileInputStream(absoluteFilePath)).use {
            inputStream ->

            GZIPOutputStream(outputStream, bufferSize).use {

                gzipOutputStream ->

                var len = inputStream.read(buffer)
                while (len >= 0)
                {
                    gzipOutputStream.write(buffer, 0, len)
                    len = inputStream.read(buffer)
                }
            }
        }
    }

    override fun fileExists(absoluteFilePath: String): Boolean
    {
        return File(absoluteFilePath).exists()
    }
}
