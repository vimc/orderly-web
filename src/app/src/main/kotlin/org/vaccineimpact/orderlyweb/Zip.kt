package org.vaccineimpact.orderlyweb

import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

interface ZipClient
{
    fun zipIt(sourceAbsolutePath: String, output: OutputStream, fileList: List<String>, gzip: Boolean = true)
    fun unzip(sourceFile: File, targetDirectory: File)
}

class Zip : ZipClient
{
    companion object {
        const val BUFFER_SIZE = 8000
        const val BUFFER_ARRAY_SIZE = 1024
    }

    val logger = LoggerFactory.getLogger(Zip::class.java)

    override fun zipIt(sourceAbsolutePath: String, output: OutputStream, fileList: List<String>, gzip: Boolean)
    {
        val source = File(sourceAbsolutePath)
        val stream = if (gzip) GZIPOutputStream(output, BUFFER_SIZE) else output

        ZipOutputStream(stream).use {

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

    override fun unzip(sourceFile: File, targetDirectory: File)
    {
        val zip = ZipFile(sourceFile)
        val entries = zip.entries()
        while (entries.hasMoreElements())
        {
            val entry = entries.nextElement()
            val newFile = File(targetDirectory, entry.name)
            if (!entry.isDirectory)
            {
                newFile.mkdirs()
                Files.copy(zip.getInputStream(entry), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
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

        val buffer = ByteArray(BUFFER_ARRAY_SIZE)

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

}
