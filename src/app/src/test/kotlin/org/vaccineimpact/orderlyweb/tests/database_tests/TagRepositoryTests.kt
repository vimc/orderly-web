package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_TAG
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_VERSION_TAG
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebTagRepository
import org.vaccineimpact.orderlyweb.models.ReportVersionTags
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.test_helpers.insertReportTags
import org.vaccineimpact.orderlyweb.test_helpers.insertVersionTags

class TagRepositoryTests : CleanDatabaseTests()
{
    @Test
    fun `can get all report tags`()
    {
        insertReport("r1", "v1")
        insertReportTags("r1", "c-tag", "b-tag", "a-tag")

        insertReport("r2", "v2")
        insertReportTags("r2", "d-tag", "c-tag")

        insertReport("r3", "v3")

        insertReport("not-returned", "v4")
        insertReportTags("not-returned", "nor-returned-tag")

        val sut = OrderlyWebTagRepository()

        val result = sut.getReportTags(listOf("r1", "r2"))

        assertThat(result.keys.count()).isEqualTo(2)
        assertThat(result["r1"]).containsExactlyElementsOf(listOf("a-tag", "b-tag", "c-tag"))
        assertThat(result["r2"]).containsExactlyElementsOf(listOf("c-tag", "d-tag"))
    }

    @Test
    fun `can update tags`()
    {
        insertReport("r1", "v1")
        insertReportTags("r1", "old-report-tag")
        insertVersionTags("v1", "old-version-tag")
        val sut = OrderlyWebTagRepository()
        val reportVersionTags = ReportVersionTags(listOf("test-version-tag"),
                listOf("test-report-tag"),
                listOf("test-orderly-tag"))

        sut.updateTags("r1", "v1", reportVersionTags)

        val reportTags = JooqContext().use {
            it.dsl.select(ORDERLYWEB_REPORT_TAG.TAG)
                    .from(ORDERLYWEB_REPORT_TAG)
                    .where(ORDERLYWEB_REPORT_TAG.REPORT.eq("r1"))
                    .fetchInto(String::class.java)
        }

        assertThat(reportTags).containsExactly("test-report-tag")

        val versionTags = JooqContext().use {
            it.dsl.select(ORDERLYWEB_REPORT_VERSION_TAG.TAG)
                    .from(ORDERLYWEB_REPORT_VERSION_TAG)
                    .where(ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION.eq("v1"))
                    .fetchInto(String::class.java)
        }

        assertThat(versionTags).containsExactly("test-version-tag")
    }


}
