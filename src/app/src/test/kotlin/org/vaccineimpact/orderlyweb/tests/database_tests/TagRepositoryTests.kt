package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_TAG
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_VERSION_TAG
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebTagRepository
import org.vaccineimpact.orderlyweb.models.ReportVersionTags
import org.vaccineimpact.orderlyweb.test_helpers.*

class TagRepositoryTests : CleanDatabaseTests()
{
    @Test
    fun `can get all tags`()
    {
        insertReport("r1", "v1")
        insertReport("r2", "v2")
        insertReportTags("r1", "c-tag", "b-tag", "a-tag")
        insertVersionTags("v1", "d-tag", "c-tag")
        insertOrderlyTags("v2", "e-tag", "f-tag")

        val sut = OrderlyWebTagRepository()

        val result = sut.getAllTags()
        assertThat(result).containsExactly("a-tag", "b-tag", "c-tag", "d-tag", "e-tag", "f-tag")
    }

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

    @Test
    fun `can get version tags`()
    {
        insertReport("report", "v1")
        insertVersionTags("v1", "c-tag", "a-tag", "b-tag")

        insertReport("report", "v2")
        insertVersionTags("v2", "aa-tag")

        insertReport("report", "v3")

        val sut = OrderlyWebTagRepository()
        val result = sut.getVersionTags(listOf("v1", "v2"))

        assertThat(result.keys).containsExactly("v1", "v2")
        assertThat(result["v1"]).containsExactlyElementsOf(listOf("a-tag", "b-tag", "c-tag"))
        assertThat(result["v2"]).containsExactlyElementsOf(listOf("aa-tag"))
    }

    @Test
    fun `can get report tags for versions`()
    {
        insertReport("report", "v1")
        insertReportTags("report", "d-tag", "b-tag")
        insertVersionTags("v1", "c-tag", "a-tag", "b-tag")

        insertReport("report2", "v2")
        insertVersionTags("v2", "aa-tag")

        insertReport("report3", "v3")
        insertReportTags("report3", "a-tag")

        val sut = OrderlyWebTagRepository()
        val result = sut.getReportTagsForVersions(listOf("v1", "v2", "v3"))

        assertThat(result.keys).containsExactly("v1", "v3")
        assertThat(result["v1"]).containsExactlyElementsOf(listOf("b-tag", "d-tag"))
        assertThat(result["v3"]).containsExactlyElementsOf(listOf("a-tag"))
    }

    @Test
    fun `can get orderly tags for versions`()
    {
        insertReport("report", "v1")
        insertVersionTags("v1", "a", "c")
        insertOrderlyTags("v1", "b", "d")

        insertReport("report2", "v2")
        insertReportTags("report2", "e")
        insertOrderlyTags("v2", "f", "e")

        insertReport("report3", "v3")
        insertOrderlyTags("v3", "g")

        val sut = OrderlyWebTagRepository()
        val result = sut.getOrderlyTagsForVersions(listOf("v1", "v2"))

        assertThat(result.keys).containsExactly("v1", "v2")
        assertThat(result["v1"]).containsExactlyElementsOf(listOf("b", "d"))
        assertThat(result["v2"]).containsExactlyElementsOf(listOf("f", "e"))
    }
}
