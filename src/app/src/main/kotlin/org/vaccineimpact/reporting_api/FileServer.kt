package org.vaccineimpact.reporting_api

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.OutputStream

interface FileSystem
{
    fun writeFileToOutputStream(absoluteFilePath: String, outputStream: OutputStream)
}

class Files: FileSystem {

    override fun writeFileToOutputStream(absoluteFilePath: String, outputStream: OutputStream) {
        val buffer = ByteArray(1024)

        BufferedInputStream(FileInputStream(absoluteFilePath)).use {
            inputStream ->

            BufferedOutputStream(outputStream).use {

                bufferedOutputStream ->

                var len = inputStream.read(buffer)
                while (len >= 0) {
                    bufferedOutputStream.write(buffer, 0, len)
                    len = inputStream.read(buffer)
                }
            }
        }
    }
}
