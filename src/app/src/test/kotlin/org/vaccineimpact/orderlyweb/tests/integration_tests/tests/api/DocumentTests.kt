package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_DOCUMENT
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class DocumentTests: IntegrationTest()
{
    @Test
    fun `can refresh documents`()
    {
        val response = apiRequestHelper.post("/documents/refresh/",
                mapOf("url" to "https://github.com/vimc/orderly-web/raw/mrc-1458/testdata/test.zip"))
        assertThat(response.statusCode).isEqualTo(200)

        JooqContext().use {
            val result = it.dsl.selectFrom(ORDERLYWEB_DOCUMENT)
                    .orderBy(ORDERLYWEB_DOCUMENT.PATH)
                    .fetch()

            assertThat(result.count()).isEqualTo(4)

            assertThat(result[0][ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/testdata")
            assertThat(result[0][ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("testdata")
            assertThat(result[0][ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(0)
            assertThat(result[0][ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo(null)
            assertThat(result[0][ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)

            assertThat(result[1][ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/testdata/subdir")
            assertThat(result[1][ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("subdir")
            assertThat(result[1][ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(0)
            assertThat(result[1][ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/testdata")
            assertThat(result[1][ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)

            assertThat(result[2][ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/testdata/subdir/test.csv")
            assertThat(result[2][ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("test.csv")
            assertThat(result[2][ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(1)
            assertThat(result[2][ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/testdata/subdir")
            assertThat(result[2][ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)

            assertThat(result[3][ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/testdata/test.doc")
            assertThat(result[3][ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("test.doc")
            assertThat(result[3][ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(1)
            assertThat(result[3][ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/testdata")
            assertThat(result[3][ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)
        }
    }
}