package org.vaccineimpact.orderlyweb.tests.unit_tests

import org.junit.Test
import org.vaccineimpact.orderlyweb.Files
import org.vaccineimpact.orderlyweb.Zip
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.test_helpers.DatabaseTests
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