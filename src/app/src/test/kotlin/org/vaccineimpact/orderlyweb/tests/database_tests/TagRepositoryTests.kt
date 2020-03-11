package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_TAG
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_VERSION_TAG
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebTagRepository
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
    fun `can tag report`()
    {
        insertReport("r1", "v1")
        val sut = OrderlyWebTagRepository()
        sut.tagReport("r1", "test-tag")

        val tags = JooqContext().use {
            it.dsl.select(ORDERLYWEB_REPORT_TAG.TAG)
                    .from(ORDERLYWEB_REPORT_TAG)
                    .where(ORDERLYWEB_REPORT_TAG.REPORT.eq("r1"))
                    .fetchInto(String::class.java)
        }

        assertThat(tags).containsExactly("test-tag")
    }

    @Test
    fun `can tag version`()
    {
        insertReport("r1", "v1")
        val sut = OrderlyWebTagRepository()
        sut.tagVersion("v1", "test-tag")

        val tags = JooqContext().use {
            it.dsl.select(ORDERLYWEB_REPORT_VERSION_TAG.TAG)
                    .from(ORDERLYWEB_REPORT_VERSION_TAG)
                    .where(ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION.eq("v1"))
                    .fetchInto(String::class.java)
        }

        assertThat(tags).containsExactly("test-tag")
    }

    @Test
    fun `can delete report tag`()
    {
        insertReport("r1", "v1")
        insertReportTags("r1", "test-tag")
        val sut = OrderlyWebTagRepository()

        sut.deleteReportTag("r1", "test-tag")

        val tags = JooqContext().use {
            it.dsl.select(ORDERLYWEB_REPORT_TAG.TAG)
                    .from(ORDERLYWEB_REPORT_TAG)
                    .where(ORDERLYWEB_REPORT_TAG.REPORT.eq("r1"))
                    .fetchInto(String::class.java)
        }

        assertThat(tags.count()).isEqualTo(0)
    }

    @Test
    fun `can delete version tag`()
    {
        insertReport("r1", "v1")
        insertVersionTags("v1", "test-tag")
        val sut = OrderlyWebTagRepository()

        sut.deleteVersionTag("v1", "test-tag")

        val tags = JooqContext().use {
            it.dsl.select(ORDERLYWEB_REPORT_VERSION_TAG.TAG)
                    .from(ORDERLYWEB_REPORT_VERSION_TAG)
                    .where(ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION.eq("v1"))
                    .fetchInto(String::class.java)
        }

        assertThat(tags.count()).isEqualTo(0)
    }

}
