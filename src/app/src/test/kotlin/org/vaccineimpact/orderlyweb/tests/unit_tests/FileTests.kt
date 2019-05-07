package org.vaccineimpact.orderlyweb.tests.unit_tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.Files
import org.vaccineimpact.orderlyweb.db.AppConfig

class FileTests {

    @Test
    fun `can getListOfFiles`()
    {
        val files = Files().getAllFilesInFolder("${AppConfig()["orderly.root"]}archive/use_resource")
        Assertions.assertThat(files.count()).isEqualTo(8)
    }
}