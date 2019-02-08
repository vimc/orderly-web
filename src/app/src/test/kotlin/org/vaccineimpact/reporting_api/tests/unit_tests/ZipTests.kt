package org.vaccineimpact.reporting_api.tests.unit_tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.Zip
import org.vaccineimpact.reporting_api.db.AppConfig
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.test_helpers.MontaguTests
import org.vaccineimpact.reporting_api.tests.database_tests.DatabaseTests
import java.io.ByteArrayOutputStream
import java.io.File

class ZipTests : DatabaseTests()
{

    @Test
    fun `can zip up folder`()
    {
        ByteArrayOutputStream().use {
            Zip().zipIt("${AppConfig()["orderly.root"]}archive", it, ".*")
        }
    }

    @Test
    fun `getListOfFilesToZip only includes files that match regex`()
    {
        val files = Zip().getListOfFilesToZip("${AppConfig()["orderly.root"]}archive/use_resource", ".*")
        Assertions.assertThat(files.count()).isEqualTo(7)

        val version = Orderly().getReportsByName("use_resource")[0]

        val sourcePath = "${AppConfig()["orderly.root"]}archive/use_resource/$version/"
        val subsetOfFiles = Zip().getListOfFilesToZip(sourcePath, "$sourcePath(meta/data.csv|mygraph.png)")
        Assertions.assertThat(subsetOfFiles.count()).isEqualTo(2)
    }

}