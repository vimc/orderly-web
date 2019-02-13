package org.vaccineimpact.reporting_api.tests.unit_tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.Files
import org.vaccineimpact.reporting_api.db.AppConfig

class FileTests {

    @Test
    fun `can getListOfFiles`()
    {
        val files = Files().getAllFilesInFolder("${AppConfig()["orderly.root"]}archive/use_resource")
        Assertions.assertThat(files.count()).isEqualTo(7)
    }
}