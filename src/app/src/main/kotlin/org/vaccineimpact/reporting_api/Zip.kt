package org.vaccineimpact.reporting_api

import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.util.*
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

interface ZipClient
{
    fun zipIt(sourceAbsolutePath: String, output: OutputStream)
}

class Zip : ZipClient
{
    val logger = LoggerFactory.getLogger(Zip::class.java)

    override fun zipIt(sourceAbsolutePath: String, output: OutputStream)
    {
        val source = File(sourceAbsolutePath)
        val fileList: ArrayList<String> = ArrayList<String>()

        populateFileList(source, fileList)

        val bufferSize = 8000
        ZipOutputStream(GZIPOutputStream(output, bufferSize)).use {

            zipOutputStream ->
            for (file in fileList)
            {
                val zipEntry = createNextZipEntry(file, source)
                zipOutputStream.putNextEntry(zipEntry)

                writeZipEntry(zipOutputStream, file)
            }

            zipOutputStream.closeEntry()
        }
    }

    private fun createNextZipEntry(absoluteFilePath: String, source: File): ZipEntry
    {
        val qualifiedFileName = source.name +
                absoluteFilePath.substring(source.absolutePath.length, absoluteFilePath.length)

        logger.debug("Creating zip entry from path $absoluteFilePath with name $qualifiedFileName")

        return ZipEntry(qualifiedFileName)
    }

    private fun writeZipEntry(zipOutputStream: ZipOutputStream, absoluteFilePath: String)
    {

        val buffer = ByteArray(1024)

        BufferedInputStream(FileInputStream(absoluteFilePath)).use {

            bufferedInputStream ->

            logger.debug("Writing zip entry from $absoluteFilePath")

            var len = bufferedInputStream.read(buffer)
            while (len > 0)
            {
                zipOutputStream.write(buffer, 0, len)
                len = bufferedInputStream.read(buffer)
            }
        }
    }

    private fun populateFileList(node: File, fileList: ArrayList<String>)
    {

        if (node.isFile)
        {
            fileList.add(node.absolutePath.toString())
        }

        if (node.isDirectory)
        {
            val subNodes = node.list()
            for (filename in subNodes)
            {
                populateFileList(File(node, filename), fileList)
            }
        }
    }

}
