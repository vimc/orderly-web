package org.vaccineimpact.orderlyweb.tests.unit_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.vaccineimpact.orderlyweb.DocumentDetails
import org.vaccineimpact.orderlyweb.Files
import org.vaccineimpact.orderlyweb.db.AppConfig
import java.io.File

class FileTests
{

    @After
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
        assertThat(children[0]).isEqualToComparingFieldByField(DocumentDetails("child", "$root/child", "/child", false, false))
        assertThat(children[1])
                .isEqualToComparingFieldByField(
                        DocumentDetails("childFile.csv", "$root/childFile.csv", "/childFile.csv", true, false))
        assertThat(children[2])
                .isEqualToComparingFieldByField(
                        DocumentDetails("https://external.com", "$root/link.web.url", "/link.web.url", true, true))
    }

    @Test
    fun `can get absolute path`()
    {
        assertThat(Files().getAbsolutePath("${AppConfig()["orderly.root"]}archive/use_resource/"))
                .isEqualTo(useResourceDir)
    }
}