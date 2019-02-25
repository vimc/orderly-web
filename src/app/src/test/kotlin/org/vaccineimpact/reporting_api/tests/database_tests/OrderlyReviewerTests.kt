package org.vaccineimpact.reporting_api.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.tests.insertReport

class OrderlyReviewerTests : CleanDatabaseTests()
{

    private fun createSut(): Orderly
    {
        return Orderly(true)
    }

    @Test
    fun `can get all published and unpublished reports`()
    {
        insertReport("test", "va")
        insertReport("test", "vz")
        insertReport("test2", "vc")
        insertReport("test2", "vb")
        insertReport("test2", "vd", published = false)
        insertReport("test3", "test3version", published = false)

        val sut = createSut()

        val results = sut.getAllReports()

        assertThat(results.count()).isEqualTo(3)

        assertThat(results[0].name).isEqualTo("test")

        assertThat(results[1].name).isEqualTo("test2")
        assertThat(results[1].latestVersion).isEqualTo("vd")

        assertThat(results[2].name).isEqualTo("test3")
    }


    @Test
    fun `can get all published and unpublished report versions`()
    {
        insertReport("test", "va")
        insertReport("test", "vz")
        insertReport("test2", "vc")
        insertReport("test2", "vb")
        insertReport("test2", "vd")
        insertReport("test3", "test3version")
        insertReport("test3", "test3versionunpublished", published = false)

        val sut = createSut()

        val results = sut.getAllReportVersions()

        assertThat(results.count()).isEqualTo(7)

        assertThat(results[0].name).isEqualTo("test")
        assertThat(results[0].displayName).isEqualTo("display name test")
        assertThat(results[0].latestVersion).isEqualTo("vz")
        assertThat(results[0].id).isEqualTo("va")
        assertThat(results[0].published).isTrue()
        assertThat(results[0].author).isEqualTo("author authorson")
        assertThat(results[0].requester).isEqualTo("requester mcfunder")

        assertThat(results[1].name).isEqualTo("test")
        assertThat(results[1].id).isEqualTo("vz")
        assertThat(results[1].latestVersion).isEqualTo("vz")

        assertThat(results[2].name).isEqualTo("test2")
        assertThat(results[2].id).isEqualTo("vb")
        assertThat(results[2].latestVersion).isEqualTo("vd")

        assertThat(results[3].name).isEqualTo("test2")
        assertThat(results[3].id).isEqualTo("vc")
        assertThat(results[3].latestVersion).isEqualTo("vd")

        assertThat(results[4].name).isEqualTo("test2")
        assertThat(results[4].id).isEqualTo("vd")
        assertThat(results[4].latestVersion).isEqualTo("vd")

        assertThat(results[5].name).isEqualTo("test3")
        assertThat(results[5].id).isEqualTo("test3version")
        assertThat(results[5].latestVersion).isEqualTo("test3versionunpublished")

        assertThat(results[6].name).isEqualTo("test3")
        assertThat(results[6].id).isEqualTo("test3versionunpublished")
        assertThat(results[6].latestVersion).isEqualTo("test3versionunpublished")
    }

    @Test
    fun `can get unpublished report metadata`()
    {
        insertReport("test", "version1",
                hashArtefacts = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\"}", published = false)

        val sut = createSut()

        val result = sut.getDetailsByNameAndVersion("test", "version1")

        assertThat(result.name).isEqualTo("test")
        assertThat(result.id).isEqualTo("version1")
    }


    @Test
    fun `can get all published and unpublished report versions for report`()
    {

        insertReport("test", "version1")
        insertReport("test", "version2")
        insertReport("test", "version3", published = false)

        val sut = createSut()

        val results = sut.getReportsByName("test")

        assertThat(results.count()).isEqualTo(3)
        assertThat(results[0]).isEqualTo("version1")
        assertThat(results[1]).isEqualTo("version2")
        assertThat(results[2]).isEqualTo("version3")

    }


}