package org.vaccineimpact.orderlyweb

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.util.zip.GZIPOutputStream

interface FileSystem
{
    fun writeFileToOutputStream(absoluteFilePath: String, outputStream: OutputStream)
    fun fileExists(absoluteFilePath: String): Boolean
    fun getAllFilesInFolder(sourceAbsolutePath: String): ArrayList<String>
    fun getChildFolders(sourceAbsolutePath: String): List<String>
    fun getChildFiles(sourceAbsolutePath: String): List<String>
}

class Files : FileSystem
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

    override fun getChildFolders(sourceAbsolutePath: String): List<String>
    {
        val source = File(sourceAbsolutePath)
        val children = source.list()
        return children.map{ File(source, it) }
                .filter{ it.isDirectory }
                .map{ it.absolutePath.toString()}
    }

    override fun getChildFiles(sourceAbsolutePath: String): List<String>
    {
        val source = File(sourceAbsolutePath)
        val children = source.list()
        return children.map{ File(source, it) }
                .filter{ it.isFile }
                .map{ it.absolutePath.toString()}
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
