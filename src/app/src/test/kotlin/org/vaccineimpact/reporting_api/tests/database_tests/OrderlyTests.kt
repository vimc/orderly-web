package org.vaccineimpact.reporting_api.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import org.vaccineimpact.reporting_api.tests.insertReport
import java.time.Instant

class OrderlyTests : DatabaseTests()
{

    private fun createSut(): Orderly
    {
        return Orderly(false)
    }

    @Test
    fun `can get all published report names`()
    {
        insertReport("test", "va")
        insertReport("test", "vz")
        insertReport("test2", "vc")
        insertReport("test2", "vb")
        insertReport("test2", "vd", published = false)
        insertReport("test3", "test3version", published = false)

        val sut = createSut()

        val results = sut.getAllReports()

        assertThat(results.count()).isEqualTo(2)

        assertThat(results[0].name).isEqualTo("test")
        assertThat(results[0].displayName).isEqualTo("display name test")
        assertThat(results[0].latestVersion).isEqualTo("vz")
        assertThat(results[0].published).isTrue()

        assertThat(results[1].name).isEqualTo("test2")
        assertThat(results[1].displayName).isEqualTo("display name test2")
        assertThat(results[1].latestVersion).isEqualTo("vb")
        assertThat(results[0].published).isTrue()
    }

    @Test
    fun `can get report metadata`()
    {

        insertReport("test", "version1",
                hashArtefacts = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\"}")

        val sut = createSut()

        val result = sut.getReportsByNameAndVersion("test", "version1")

        assertThat(result.has("name")).isTrue()
        assertThat(result.has("id")).isTrue()

        assertThat(result.has("hash_artefacts")).isTrue()
        assertThat(result["hash_artefacts"].asJsonObject.has("summary.csv")).isTrue()
    }

    @Test
    fun `throws unknown object error if report version not published`()
    {

        insertReport("test", "version1", published = false)

        val sut = createSut()

        assertThatThrownBy { sut.getReportsByNameAndVersion("test", "version1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `throws unknown object error if report version doesnt exist`()
    {

        insertReport("test", "version1",
                hashArtefacts = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\"}")

        val sut = createSut()

        assertThatThrownBy { sut.getReportsByNameAndVersion("test", "dsajkdsj") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `throws unknown object error if report name not found`()
    {

        insertReport("test", "version1")

        val sut = createSut()

        assertThatThrownBy { sut.getReportsByNameAndVersion("dsajkdsj", "version") }
                .isInstanceOf(UnknownObjectError::class.java)

    }

    @Test
    fun `can get all published report versions`()
    {

        insertReport("test", "version1")
        insertReport("test", "version2")
        insertReport("test", "version3", published = false)

        val sut = createSut()

        val results = sut.getReportsByName("test")

        assertThat(results.count()).isEqualTo(2)
        assertThat(results[0]).isEqualTo("version1")
        assertThat(results[1]).isEqualTo("version2")

    }


}