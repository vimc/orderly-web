package org.vaccineimpact.orderlyweb.tests.unit_tests

import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.vaccineimpact.orderlyweb.Files
import org.vaccineimpact.orderlyweb.db.AppConfig
import java.io.File

class FileTests {

    private val useResourceDir = File("${AppConfig()["orderly.root"]}archive/use_resource/").absolutePath

    @Test
    fun `can getListOfFiles`()
    {
        val files = Files().getAllFilesInFolder(useResourceDir)
        assertThat(files.count()).isEqualTo(7)
    }

    @Test
    fun `can getChildFiles`()
    {
        val folders = Files().getChildFolders(useResourceDir)
        val files = Files().getChildFiles(folders[0])
        assertThat(files.count()).isEqualTo(6)
        assertThat(files).contains("${folders[0]}/mygraph.png")
        assertThat(files).contains("${folders[0]}/orderly.yml")
    }

    @Test
    fun `can getChildFolders`()
    {
        //expect a single child folder
        val folders = Files().getChildFolders(useResourceDir)
        assertThat(folders.count()).isEqualTo(1)
        assertThat(folders[0]).startsWith(useResourceDir)
    }
}