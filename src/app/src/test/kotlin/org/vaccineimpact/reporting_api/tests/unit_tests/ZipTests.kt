package org.vaccineimpact.reporting_api.tests.unit_tests

import org.junit.Test
import org.vaccineimpact.reporting_api.Files
import org.vaccineimpact.reporting_api.Zip
import org.vaccineimpact.reporting_api.db.AppConfig
import org.vaccineimpact.reporting_api.tests.database_tests.DatabaseTests
import java.io.ByteArrayOutputStream

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

}