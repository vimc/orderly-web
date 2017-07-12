package org.vaccineimpact.reporting_api.tests.controllers

import org.junit.Test
import org.vaccineimpact.reporting_api.Zip
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.test_helpers.MontaguTests
import java.io.ByteArrayOutputStream
import java.io.File

class ZipTests: MontaguTests()
{

    @Test
    fun `can zip up folder`()
    {
        ByteArrayOutputStream().use {
            Zip().zipIt("${Config["orderly.root"]}archive", it)
        }
    }

}
