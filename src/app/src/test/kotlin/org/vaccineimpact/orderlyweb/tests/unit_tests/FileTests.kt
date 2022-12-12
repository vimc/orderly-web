package org.vaccineimpact.orderlyweb.tests.unit_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.DocumentDetails
import org.vaccineimpact.orderlyweb.Files
import org.vaccineimpact.orderlyweb.db.AppConfig
import java.io.File
import java.net.URL

class FileTests
{

    @AfterEach
    fun cleanup()
    {
        File("documents").deleteRecursively()
    }

    private val useResourceDir = File("${AppConfig()["orderly.root"]}archive/use_resource/").absolutePath
    @Test
    fun `can getListOfFiles`()
    {
        val files = Files().getAllFilesInFolder(useResourceDir)
        assertThat(files.count()).isEqualTo(7)
    }

    @Test
    fun `can getAllChildren`()
    {
        File("documents/child/grandchild").mkdirs()
        File("documents/childFile.csv").createNewFile()
        File("documents/link.web.url").createNewFile()
        File("documents/link.web.url").writeText("[InternetShortcut]\n" +
                "URL=https://external.com\n")

        val root = File("documents").absolutePath
        val children = Files().getAllChildren(root, root).sortedBy { it.name }
        assertThat(children.count()).isEqualTo(3)
        assertThat(children[0]).isEqualToComparingFieldByField(DocumentDetails("child", "child", "$root/child", "/child", false, false))
        assertThat(children[1])
                .isEqualToComparingFieldByField(
                        DocumentDetails("childFile.csv", "childFile.csv", "$root/childFile.csv", "/childFile.csv", true, false))
        assertThat(children[2])
                .isEqualToComparingFieldByField(
                        DocumentDetails("https://external.com", "link", "$root/link.web.url", "/link.web.url", true, true))
    }

    @Test
    fun `external links use file name as display name and url as name`()
    {
        File("documents").mkdirs()
        File("documents/link.web.url").createNewFile()
        File("documents/link.web.url").writeText("[InternetShortcut]\n" +
                "URL=https://external.com\n")

        val root = File("documents").absolutePath
        val children = Files().getAllChildren(root, root).sortedBy { it.name }
        assertThat(children[0])
                .isEqualToComparingFieldByField(
                        DocumentDetails("https://external.com", "link", "$root/link.web.url", "/link.web.url", true, true))
    }

    @Test
    fun `can get absolute path`()
    {
        assertThat(Files().getAbsolutePath("${AppConfig()["orderly.root"]}archive/use_resource/"))
                .isEqualTo(useResourceDir)
    }

    @Test
    fun `can save zip from url`() {

        val testDir = java.nio.file.Files.createTempDirectory("test").toFile()
        Files().saveArchiveFromUrl(URL("https://github.com/vimc/orderly-web/raw/mrc-1458/testdata/test.zip"), testDir.absolutePath)
        assertThat(File(testDir, "testdata/test.doc").isFile).isTrue()
        assertThat(File(testDir, "testdata/subdir").isDirectory).isTrue()
        assertThat(File(testDir, "testdata/subdir/test.csv").isFile).isTrue()
    }
}