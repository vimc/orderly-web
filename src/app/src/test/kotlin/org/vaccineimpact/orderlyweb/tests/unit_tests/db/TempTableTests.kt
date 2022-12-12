package org.vaccineimpact.orderlyweb.tests.unit_tests.db

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.db.TempTable

class TempTableTests
{
    @Test
    fun `can get field`()
    {
        JooqContext().use {
            val select = it.dsl.select(REPORT_VERSION.ID).from(REPORT_VERSION)

            val sut = TempTable("testTemp", select)

            assertThat(sut.field<String>("ID").getName()).isEqualTo("ID")
        }
    }
}