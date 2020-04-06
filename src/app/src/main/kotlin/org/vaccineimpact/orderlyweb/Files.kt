package org.vaccineimpact.orderlyweb

import org.apache.commons.io.FileUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.URL
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipException

interface FileSystem
{
    fun writeFileToOutputStream(absoluteFilePath: String, outputStream: OutputStream)
    fun fileExists(absoluteFilePath: String): Boolean
    fun getAllFilesInFolder(sourceAbsolutePath: String): ArrayList<String>
    fun getAbsolutePath(sourcePath: String): String
    fun getAllChildren(sourceAbsolutePath: String, documentsRoot: String): List<DocumentDetails>
    @Throws(ZipException::class)
    fun save(url: URL, targetAbsolutePath: String)
}

class Files(val zip: ZipClient = Zip()) : FileSystem
{

    override fun writeFileToOutputStream(absoluteFilePath: String, outputStream: OutputStream)
    {
        val buffer = ByteArray(1024)
        val bufferSize = 8000

        BufferedInputStream(FileInputStream(absoluteFilePath)).use { inputStream ->

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


    override fun getAllFilesInFolder(sourceAbsolutePath: String): ArrayList<String>
    {
        val source = File(sourceAbsolutePath)
        val fileList = arrayListOf<String>()

        populateFileList(source, fileList)

        return fileList
    }

    override fun getAllChildren(sourceAbsolutePath: String, documentsRoot: String): List<DocumentDetails>
    {
        val source = File(sourceAbsolutePath)
        val children = source.list() ?: arrayOf()
        return children.map { File(source, it) }
                .map { getDocumentDetails(it, documentsRoot) }
    }

    override fun save(url: URL, targetAbsolutePath: String)
    {
        val tmpFile = java.nio.file.Files.createTempFile("documents", ".zip").toFile()

        FileUtils.copyURLToFile(
                url,
                tmpFile)

        val tmpDir = java.nio.file.Files.createTempDirectory("documents").toFile()

        zip.unzip(tmpFile, tmpDir)

        val targetDir = File(targetAbsolutePath)
        targetDir.mkdirs()

        FileUtils.cleanDirectory(targetDir)
        FileUtils.copyDirectory(tmpDir, targetDir)
    }

    private fun getDocumentDetails(file: File, documentsRoot: String): DocumentDetails
    {
        return if (file.extension == "url")
        {
            val url = file.readText().trim('\n').trim().split("URL=").last()
            val name = file.name.substringBefore(".web.url") // don't include extension in name
            DocumentDetails(url, name, file.absolutePath, file.absolutePath.removePrefix(documentsRoot), true, true)
        }
        else
        {
            DocumentDetails(file.name,
                    file.name,
                    file.absolutePath,
                    file.absolutePath.removePrefix(documentsRoot),
                    file.isFile,
                    false)
        }
    }

    override fun getAbsolutePath(sourcePath: String): String
    {
        return File(sourcePath).absolutePath
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

class DocumentDetails(val name: String,
                      val displayName: String,
                      val absolutePath: String,
                      val pathFragment: String?,
                      val isFile: Boolean,
                      val external: Boolean)
