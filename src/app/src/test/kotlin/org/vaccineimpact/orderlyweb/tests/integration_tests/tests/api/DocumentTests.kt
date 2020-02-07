package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_DOCUMENT
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import java.io.File

class DocumentTests: IntegrationTest()
{
    @After
    fun cleanup()
    {
        File("documents").deleteRecursively()
    }

    @Before
    fun setup() {
        File("documents/some/path").mkdirs()
        File("documents/some/path/file.csv").createNewFile()
    }

    @Test
    fun `can refresh documents`()
    {
        val response = apiRequestHelper.post("/documents/refresh/", mapOf())
        assertThat(response.statusCode).isEqualTo(200)

        JooqContext().use {
            val result = it.dsl.selectFrom(ORDERLYWEB_DOCUMENT)
                    .orderBy(ORDERLYWEB_DOCUMENT.PATH)
                    .fetch()

            assertThat(result.count()).isEqualTo(4)

            assertThat(result[0][ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/")
            assertThat(result[0][ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("documents")
            assertThat(result[0][ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(0)
            assertThat(result[0][ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo(null)
            assertThat(result[0][ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)

            assertThat(result[1][ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/some/")
            assertThat(result[1][ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("some")
            assertThat(result[1][ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(0)
            assertThat(result[1][ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/")
            assertThat(result[1][ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)

            assertThat(result[2][ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/some/path/")
            assertThat(result[2][ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("path")
            assertThat(result[2][ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(0)
            assertThat(result[2][ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/some/")
            assertThat(result[2][ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)

            assertThat(result[3][ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/some/path/file.csv")
            assertThat(result[3][ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("file.csv")
            assertThat(result[3][ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(1)
            assertThat(result[3][ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/some/path/")
            assertThat(result[3][ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)
        }
    }
}