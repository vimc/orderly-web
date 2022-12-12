package org.vaccineimpact.orderlyweb.tests.unit_tests

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.Files
import org.vaccineimpact.orderlyweb.Zip
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.test_helpers.DatabaseTests
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ZipTests : DatabaseTests()
{

    @Test
    fun `can zip up folder`()
    {
        val files = Files().getAllFilesInFolder("${AppConfig()["orderly.root"]}archive/use_resource")
        ByteArrayOutputStream().use {
            Zip().zipIt("${AppConfig()["orderly.root"]}archive/use_resource", it, files)
        }
    }

    @Test
    fun `can unzip archive`()
    {
        // arrange
        val sourceDir = java.nio.file.Files.createTempDirectory("source").toFile()
        val subDir = File(sourceDir, "subdir")
        val sourceFile = File(sourceDir, "text.csv")
        val subFile = File(subDir, "test.txt")
        subDir.mkdirs()
        subFile.createNewFile()
        sourceFile.createNewFile()

        val testZip = java.nio.file.Files.createTempFile("test", ".zip").toFile()

        val sut = Zip()
        FileOutputStream(testZip).use {
            sut.zipIt(sourceDir.absolutePath, it, listOf(sourceFile.absolutePath, subFile.absolutePath), gzip = false)
        }

        val resultsDir = java.nio.file.Files.createTempDirectory("results").toFile()

        // act
        sut.unzip(testZip, resultsDir)

        // assert
        val resultSourceDir = File(resultsDir, sourceDir.name)
        assertThat(File(resultSourceDir, "subdir").isDirectory).isTrue()
        assertThat(File(resultSourceDir, "text.csv").isFile).isTrue()
        assertThat(File(resultSourceDir, "subdir/test.txt").isFile).isTrue()
    }

}