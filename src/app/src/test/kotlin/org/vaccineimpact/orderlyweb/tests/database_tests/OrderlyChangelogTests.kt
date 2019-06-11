package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Changelog
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.tests.InsertableChangelog
import org.vaccineimpact.orderlyweb.tests.insertChangelog
import org.vaccineimpact.orderlyweb.test_helpers.insertReport

class OrderlychangelogTests : CleanDatabaseTests()
{
    private fun createSut(isReviewer: Boolean = false): Orderly
    {
        return Orderly(isReviewer)
    }

    @Test
    fun `reviewer can get all changelog for report version`()
    {
        insertReport("test", "version1")
        insertChangelog(listOf(
                InsertableChangelog(
                        "zz_id1",
                        "version1",
                        "public",
                        "did something great",
                        true,
                        1),
                InsertableChangelog(
                        "id2",
                        "version1",
                        "internal",
                        "did something awful",
                        false,
                        2)))

        val sut = createSut(true)

        val results = sut.getChangelogByNameAndVersion("test", "version1")

        Assertions.assertThat(results.count()).isEqualTo(2)

        //changelog items are returned in desc order
        assertChangelogValuesMatch(results[0], "version1", "internal", "did something awful", false)
        assertChangelogValuesMatch(results[1], "version1", "public", "did something great", true)
    }

    @Test
    fun `reader can get public changelog for report version`()
    {
        insertReport("test", "version1")
        insertChangelog(listOf(
                InsertableChangelog(
                        "zz_id1",
                        "version1",
                        "public",
                        "did something great",
                        true,
                        1,
                        "version1"),
                InsertableChangelog(
                        "id2",
                        "version1",
                        "internal",
                        "did something awful",
                        false,
                        2,
                        "version1")))

        val sut = createSut(false)

        val results = sut.getChangelogByNameAndVersion("test", "version1")

        Assertions.assertThat(results.count()).isEqualTo(1)

        assertChangelogValuesMatch(results[0], "version1", "public", "did something great", true)
    }

    @Test
    fun `reviewer can get all changelog for previous report versions`()
    {
        insertTestReportChangelogs()

        val sut = createSut(true)

        val results = sut.getChangelogByNameAndVersion("test", "version3")

        assertExpectedTestChangelogValues("version3", results)

    }

    @Test
    fun `reader can get public changelog for previous report versions`()
    {
        insertTestReportChangelogs()

        val sut = createSut(false)

        val results = sut.getChangelogByNameAndVersion("test", "version3")

        assertExpectedPublicTestChangelogValues("version3", results)

    }

    @Test
    fun `reviewer does not get changelog for later report versions`()
    {
        insertTestReportChangelogs()
        val sut = createSut(true)

        val results = sut.getChangelogByNameAndVersion("test", "version2")

        assertExpectedTestChangelogValues("version2", results)
    }

    @Test
    fun `reader does not get changelog for later report versions`()
    {
        insertTestReportChangelogs()
        val sut = createSut(false)

        val results = sut.getChangelogByNameAndVersion("test", "version1")

        assertExpectedPublicTestChangelogValues("version1", results)
    }

    @Test
    fun `reviewer does not get changelog for other reports`()
    {
        insertTestReportChangelogs()

        insertReport("anothertest", "anotherversion1")
        insertChangelog(listOf(
                InsertableChangelog(
                        "id7",
                        "anotherversion1",
                        "public",
                        "did something great v1",
                        true,
                        7),
                InsertableChangelog(
                        "id8",
                        "anotherversion1",
                        "internal",
                        "did something awful v1",
                        false,
                        8)))

        val sut = createSut(true)

        val results = sut.getChangelogByNameAndVersion("test", "version2")

        assertExpectedTestChangelogValues("version2", results)
    }

    @Test
    fun `reader does not get changelog for other reports`()
    {
        insertTestReportChangelogs()

        insertReport("anothertest", "anotherversion1")
        insertChangelog(listOf(
                InsertableChangelog(
                        "id7",
                        "anotherversion1",
                        "public",
                        "did something great v1",
                        true,
                        7),
                InsertableChangelog(
                        "id8",
                        "anotherversion1",
                        "internal",
                        "did something awful v1",
                        false,
                        8)))

        val sut = createSut(false)

        val results = sut.getChangelogByNameAndVersion("test", "version3")

        assertExpectedPublicTestChangelogValues("version3", results)
    }

    @Test
    fun `can get empty changelog`()
    {
        insertReport("emptytest", "version1")

        val sut = createSut()

        val results = sut.getChangelogByNameAndVersion("emptytest", "version1")

        Assertions.assertThat(results.count()).isEqualTo(0)
    }

    @Test
    fun `throws unknown object error when getting changelog for nonexistent version`()
    {

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getChangelogByNameAndVersion("test", "versionX") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `throws unknown object error when getting changelog for version which is not in named report`()
    {
        insertTestReportChangelogs()

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getChangelogByNameAndVersion("anothertest", "version1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `throws unknown object error when reader attempts to get changelog for version which in not published`()
    {
        insertTestReportChangelogs()

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getChangelogByNameAndVersion("test", "version2") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `throws unknown object error when getting latest changelog for nonexistent report`()
    {

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getLatestChangelogByName("does not exist") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `reviewer can get latest changelog for report`()
    {
        insertTestReportChangelogs()

        val sut = createSut(true)

        val results = sut.getLatestChangelogByName("test")

        assertExpectedTestChangelogValues("version3", results)
    }

    @Test
    fun `reader can get latest public changelog for report`()
    {
        insertTestReportChangelogs()

        val sut = createSut(false)

        val results = sut.getLatestChangelogByName("test")

        assertExpectedPublicTestChangelogValues("version3", results)
    }

    @Test
    fun `reviewer does not get latest changelog for other reports`()
    {
        insertTestReportChangelogs()

        insertReport("anothertest", "anotherversion1")
        insertChangelog(listOf(
                InsertableChangelog(
                        "id7",
                        "anotherversion1",
                        "public",
                        "did something great v1",
                        true,
                        7,
                        "anotherversion1"),
                InsertableChangelog(
                        "id8",
                        "anotherversion1",
                        "internal",
                        "did something awful v1",
                        false,
                        8,
                        "anotherversion1")))

        val sut = createSut(true)

        val results = sut.getLatestChangelogByName("test")

        assertExpectedTestChangelogValues("version3", results)
    }

    @Test
    fun `reader does not get latest changelog for other reports`()
    {
        insertTestReportChangelogs()

        insertReport("anothertest", "anotherversion1")
        insertChangelog(listOf(
                InsertableChangelog(
                        "id7",
                        "anotherversion1",
                        "public",
                        "did something great v1",
                        true,
                        7,
                        "anotherversion1"),
                InsertableChangelog(
                        "id8",
                        "anotherversion1",
                        "internal",
                        "did something awful v1",
                        false,
                        8,
                        "anotherversion1")))

        val sut = createSut(false)

        val results = sut.getLatestChangelogByName("test")

        assertExpectedPublicTestChangelogValues("version3", results)
    }

    @Test
    fun `getLatestChangelogByName returns changelog for latest published version to reader`()
    {
        // old unpublished version with changelogs forwarded to next published version
        insertReport("test", "v1", published = false)
        // old published version
        insertReport("test", "v2", published = false)
        // latest published version
        insertReport("test", "v3", published = true)
        // latest unpublished version
        insertReport("test", "v4", published = false)

        insertChangelog(listOf(
                InsertableChangelog(
                        "id1",
                        "v1",
                        "public",
                        "did something great v1",
                        true,
                        1,
                        reportVersionPublic = "v2"),
                InsertableChangelog(
                        "id2",
                        "v1",
                        "internal",
                        "did something awful v1",
                        false,
                        2,
                        reportVersionPublic = "v2")))

        insertChangelog(listOf(
                InsertableChangelog(
                        "id3",
                        "v2",
                        "public",
                        "did something great v2",
                        true,
                        3,
                        reportVersionPublic = "v2"),
                InsertableChangelog(
                        "id4",
                        "v2",
                        "internal",
                        "did something awful v2",
                        false,
                        4,
                        reportVersionPublic = "v2")))

        insertChangelog(listOf(
                InsertableChangelog(
                        "id5",
                        "v3",
                        "public",
                        "did something great v3",
                        true,
                        5,
                        reportVersionPublic = "v3"),
                InsertableChangelog(
                        "id6",
                        "v3",
                        "internal",
                        "did something awful v3",
                        false,
                        6,
                        reportVersionPublic = "v3")))

        insertChangelog(listOf(
                InsertableChangelog(
                        "id7",
                        "v4",
                        "public",
                        "did something great v4",
                        true,
                        7,
                        reportVersionPublic = null),
                InsertableChangelog(
                        "id8",
                        "v4",
                        "internal",
                        "did something awful v4",
                        false,
                        8,
                        reportVersionPublic = null)))

        val sut = createSut(false)

        val results = sut.getLatestChangelogByName("test")

        Assertions.assertThat(results.count()).isEqualTo(3)

        assertChangelogValuesMatch(results[0], "v3", "public", "did something great v3", true)
        assertChangelogValuesMatch(results[1], "v2", "public", "did something great v2", true)
        assertChangelogValuesMatch(results[2], "v2", "public", "did something great v1", true)
    }

    private fun insertTestReportChangelogs()
    {
        insertReport("test", "version1")
        insertReport("test", "version2", published = false)
        insertReport("test", "version3")

        insertChangelog(listOf(
                InsertableChangelog(
                        "id1",
                        "version1",
                        "public",
                        "did something great v1",
                        true,
                        1,
                        null),
                InsertableChangelog(
                        "id2",
                        "version1",
                        "internal",
                        "did something awful v1",
                        false,
                        2,
                        null)))

        insertChangelog(listOf(
                InsertableChangelog(
                        "id3",
                        "version2",
                        "public",
                        "did something great v2",
                        true,
                        3,
                        "version3")))

        insertChangelog(listOf(
                InsertableChangelog(
                        "id4",
                        "version3",
                        "public",
                        "did something great v3",
                        true,
                        4,
                        "version3"),
                InsertableChangelog(
                        "id5",
                        "version3",
                        "internal",
                        "did something awful v3",
                        false,
                        5,
                        "version3"),
                InsertableChangelog(
                        "id6",
                        "version3",
                        "internal",
                        "everything is broken",
                        false,
                        6,
                        "version3")))

    }

    private fun assertExpectedTestChangelogValues(latestVersion: String, results: List<Changelog>)
    {
        var index = 0

        if (latestVersion == "version3")
        {
            assertChangelogValuesMatch(results[0], "version3", "internal", "everything is broken", false)
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
            assertChangelogValuesMatch(results[index + 1], "version1", "public", "did something great v1", true)

            index += 2
        }

        Assertions.assertThat(results.count()).isEqualTo(index)

        if (index == 0)
        {
            Assertions.fail("Bad test configuration - unexpected report version when checking expected test changelog values")
        }
    }

    private fun assertExpectedPublicTestChangelogValues(latestVersion: String, results: List<Changelog>)
    {
        var index = 0

        if (latestVersion == "version3")
        {
            assertChangelogValuesMatch(results[0], "version3", "public", "did something great v3", true)

            index++
        }

        if (latestVersion == "version2" || index > 0)
        {
            //The public version of this changelog item is version3, while the 'real' version is version2
            assertChangelogValuesMatch(results[index], "version3", "public", "did something great v2", true)

            index++
        }

        Assertions.assertThat(results.count()).isEqualTo(index)

        if ((index == 0) && (latestVersion != "version1"))
        {
            Assertions.fail("Bad test configuration - unexpected report version when checking expected public test changelog values")
        }
    }

    private fun assertChangelogValuesMatch(changelog: Changelog, report_version: String, label: String,
                                           value: String, fromFile: Boolean)
    {
        Assertions.assertThat(changelog.reportVersion).isEqualTo(report_version)
        Assertions.assertThat(changelog.fromFile).isEqualTo(fromFile)
        Assertions.assertThat(changelog.label).isEqualTo(label)
        Assertions.assertThat(changelog.value).isEqualTo(value)
    }
}