package org.vaccineimpact.reporting_api.tests.database_tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.api.models.Changelog
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import org.vaccineimpact.reporting_api.tests.insertReport

class OrderlyTests : DatabaseTests()
{

    private fun createSut(): Orderly
    {
        return Orderly(false)
    }

    @Test
    fun `can get all published reports`()
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
        assertThat(results[0].author).isEqualTo("author authorson")
        assertThat(results[0].requester).isEqualTo("requester mcfunder")

        assertThat(results[1].name).isEqualTo("test2")
        assertThat(results[1].displayName).isEqualTo("display name test2")
        assertThat(results[1].latestVersion).isEqualTo("vb")
        assertThat(results[1].published).isTrue()
    }

    @Test
    fun `can get all published report versions`()
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

        assertThat(results.count()).isEqualTo(6)

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
        assertThat(results[5].latestVersion).isEqualTo("test3version")

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
    fun `can get all published report versions for report`()
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

    @Test
    fun `can get changelog for report version`()
    {
        insertReport("test", "version1")

        val sut = createSut()

        val results = sut.getChangelogByNameAndVersion("test", "version1")

        assertThat(results.count()).isEqualTo(2)

        //changelog items are returned in desc order
        assertChangelogValuesMatch(results[0], "version1", "internal", "did something awful", false)
        assertChangelogValuesMatch(results[1], "version1", "public", "did something great", true)
    }

    @Test
    fun `can get changelog for previous report versions`()
    {
        insertTestReportChangelogs()

        val sut = createSut()

        val results = sut.getChangelogByNameAndVersion("test", "version3")

        assertExpectedTestChangelogValues("version3", results)

    }

    @Test
    fun `do not get changelog for later report versions`()
    {
        insertTestReportChangelogs()
        val sut = createSut()

        val results = sut.getChangelogByNameAndVersion("test", "version2")

        assertExpectedTestChangelogValues("version2", results)
    }

    @Test
    fun `do not get changelog for other reports`()
    {
        insertTestReportChangelogs()

        insertReport("anothertest", "anotherversion1", changelog = listOf(
                Changelog("anotherversion1", "public","did something great v1", true),
                Changelog("anotherversion1","internal","did something awful v1", false)))

        val sut = createSut()

        val results = sut.getChangelogByNameAndVersion("test", "version2")

        assertExpectedTestChangelogValues("version2", results)
    }


    @Test
    fun `can get empty changelog`()
    {
        insertReport("emptytest", "version1", changelog = listOf())

        val sut = createSut()

        val results = sut.getChangelogByNameAndVersion("emptytest", "version1")

        assertThat(results.count()).isEqualTo(0)
    }

    @Test
    fun `throws unknown object error when getting changelog for nonexistent version`()
    {

        val sut = createSut()

        assertThatThrownBy { sut.getChangelogByNameAndVersion("test", "versionX") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `throws unknown object error when getting changelog for version which is not in named report`()
    {
        insertTestReportChangelogs()

        val sut = createSut()

        assertThatThrownBy { sut.getChangelogByNameAndVersion("anothertest", "version1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `can get latest changelog for report`()
    {
        insertTestReportChangelogs()

        val sut = createSut()

        val results = sut.getLatestChangelogByName("test")

        assertExpectedTestChangelogValues("version3", results)
    }

    @Test
    fun `do not get latest changelog for other reports`()
    {
        insertTestReportChangelogs()

        insertReport("anothertest", "anotherversion1", changelog = listOf(
                Changelog("anotherversion1", "public","did something great v1", true),
                Changelog("anotherversion1","internal","did something awful v1", false)))

        val sut = createSut()

        val results = sut.getLatestChangelogByName("test")

        assertExpectedTestChangelogValues("version3", results)
    }

    @Test
    fun `throws unknown object error when getting latest changelog for nonexistent report`()
    {

        val sut = createSut()

        assertThatThrownBy { sut.getLatestChangelogByName("does not exist") }
                .isInstanceOf(UnknownObjectError::class.java)
    }


    private fun insertTestReportChangelogs()
    {
        insertReport("test", "version1", changelog = listOf(
                Changelog("version1", "public","did something great v1", true),
                Changelog("version1","internal","did something awful v1", false)))

        insertReport("test", "version2", changelog = listOf(
                Changelog("version2", "public","did something great v2", true)))

        insertReport("test", "version3", changelog = listOf(
                Changelog("version3", "public","did something great v3", true),
                Changelog("version3","internal","did something awful v3", false),
                Changelog("version3", "technical", "everything is broken", false)))
    }

    private fun assertExpectedTestChangelogValues(latestVersion: String, results: List<Changelog>)
    {
        var index = 0

        if (latestVersion == "version3")
        {
            assertChangelogValuesMatch(results[0], "version3", "technical", "everything is broken", false)
            assertChangelogValuesMatch(results[1], "version3", "internal", "did something awful v3", false)
            assertChangelogValuesMatch(results[2], "version3", "public", "did something great v3", true)

            index += 3
        }

        if (latestVersion == "version2" || index > 0)
        {
            assertChangelogValuesMatch(results[index], "version2", "public", "did something great v2", true)

            index++
        }

        if (latestVersion == "version1" || index > 0)
        {
            assertChangelogValuesMatch(results[index], "version1", "internal", "did something awful v1", false)
            assertChangelogValuesMatch(results[index+1], "version1", "public", "did something great v1", true)

            index += 2
        }

        assertThat(results.count()).isEqualTo(index)

        if (index == 0)
        {
            Assertions.fail("Bad test configuration - unexpected report version when checking expected test changelog values")
        }
    }

    private fun assertChangelogValuesMatch(changelog: Changelog, report_version: String, label: String,
                                           value: String, fromFile: Boolean)
    {
        assertThat(changelog.reportVersion).isEqualTo(report_version)
        assertThat(changelog.fromFile).isEqualTo(fromFile)
        assertThat(changelog.label).isEqualTo(label)
        assertThat(changelog.value).isEqualTo(value)
    }


}