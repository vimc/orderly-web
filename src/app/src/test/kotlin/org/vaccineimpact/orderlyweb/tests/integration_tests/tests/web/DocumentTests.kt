package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import java.io.File

class DocumentTests : IntegrationTest()
{
    //private val readDocuments = setOf(ReifiedPermission("documents.read", Scope.Global()))

    @After
    fun cleanup() {
        File("documents").deleteRecursively()
    }

    @Test
    fun `documents can be downloaded`()
    {
        File("documents/some/path").mkdirs()
        File("documents/some/path/file.csv").createNewFile()
        File("documents/some/path/file.csv").writeText("TEST")
        val sessionCookie = webRequestHelper.webLoginWithMontagu()
        val response = webRequestHelper.requestWithSessionCookie("/documents/some/path/file.csv", sessionCookie,
                ContentTypes.binarydata)
        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("text/csv")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=\"some/path/file.csv\"")
        Assertions.assertThat(response.text).isEqualTo("TEST")
    }
}
