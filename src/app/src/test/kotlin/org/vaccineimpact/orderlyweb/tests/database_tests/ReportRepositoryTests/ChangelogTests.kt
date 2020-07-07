package org.vaccineimpact.orderlyweb.tests.database_tests.ReportRepositoryTests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.Changelog
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.InsertableChangelog
import org.vaccineimpact.orderlyweb.tests.insertChangelog
import java.sql.Timestamp
import java.time.Instant

class ChangelogTests : CleanDatabaseTests()
{
    private fun createSut(isReviewer: Boolean = false): ReportRepository
    {
        return OrderlyReportRepository(isReviewer, true, listOf())
    }

    private val older = Instant.parse("2017-12-03T10:15:30.00Z")
    private val old = Instant.parse("2018-12-03T10:15:30.00Z")
    private val latest = Instant.parse("2019-12-03T10:15:30.00Z")

    @Test
    fun `reviewer can get all changelog`()
    {
        insertReport("test", "version1")
        insertChangelog(
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
                        2))

        val sut = createSut(true)

        val results = sut.getDatedChangelogForReport("test", Instant.now())

        Assertions.assertThat(results.count()).isEqualTo(2)

        //changelog items are returned in desc order
        assertChangelogValuesMatch(results[0], "version1", "internal", "did something awful", false, false)
        assertChangelogValuesMatch(results[1], "version1", "public", "did something great", true, true)
    }

    @Test
    fun `reader can get public changelog`()
    {
        insertReport("test", "version1")
        insertChangelog(
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
                        "version1"))

        val sut = createSut(false)

        val results = sut.getDatedChangelogForReport("test", Instant.now())

        Assertions.assertThat(results.count()).isEqualTo(1)

        assertChangelogValuesMatch(results[0], "version1", "public", "did something great", true, true)
    }

    @Test
    fun `reviewer can get all changelog for previous report versions`()
    {
        insertTestReportChangelogs()

        val sut = createSut(true)

        val results = sut.getDatedChangelogForReport("test", Instant.now())

        assertExpectedTestChangelogValues("version3", results)

    }

    @Test
    fun `reader can get public changelog for previous report versions`()
    {
        insertTestReportChangelogs()

        val sut = createSut(false)

        val results = sut.getDatedChangelogForReport("test", Instant.now())

        assertExpectedPublicTestChangelogValues("version3", results)
    }

    @Test
    fun `reviewer does not get changelog for later report versions`()
    {
        insertTestReportChangelogs()
        val sut = createSut(true)

        val results = sut.getDatedChangelogForReport("test", old)

        assertExpectedTestChangelogValues("version2", results)
    }

    @Test
    fun `reader does not get changelog for later report versions`()
    {
        insertTestReportChangelogs()
        val sut = createSut(false)

        val results = sut.getDatedChangelogForReport("test", old)

        assertExpectedPublicTestChangelogValues("version1", results)
    }

    @Test
    fun `reviewer does not get changelog for other reports`()
    {
        insertTestReportChangelogs()

        insertReport("anothertest", "anotherversion1")
        insertChangelog(
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
                        8))

        val sut = createSut(true)

        val results = sut.getDatedChangelogForReport("test", Instant.now())

        assertExpectedTestChangelogValues("version3", results)
    }

    @Test
    fun `reader does not get changelog for other reports`()
    {
        insertTestReportChangelogs()

        insertReport("anothertest", "anotherversion1")
        insertChangelog(
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
                        8))

        val sut = createSut(false)

        val results = sut.getDatedChangelogForReport("test", Instant.now())

        assertExpectedPublicTestChangelogValues("version3", results)
    }

    @Test
    fun `can get empty changelog`()
    {
        insertReport("emptytest", "version1")

        val sut = createSut()

        val results = sut.getDatedChangelogForReport("emptytest", Instant.now())

        Assertions.assertThat(results.count()).isEqualTo(0)
    }

    @Test
    fun `getLatestVersion throws unknown object error when report does not exist`()
    {
        val sut = createSut()
        Assertions.assertThatThrownBy { sut.getLatestVersion("test") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `getLatestVersion throws unknown object error for reader when report is not published`()
    {
        insertReport("test", "version1", published = false)

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getLatestVersion("test") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    private fun insertTestReportChangelogs()
    {
        insertReport("test", "version1", date = Timestamp.from(older))
        insertReport("test", "version2", published = false, date = Timestamp.from(old))
        insertReport("test", "version3", date = Timestamp.from(latest))

        insertChangelog(
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
                        null))

        insertChangelog(
                InsertableChangelog(
                        "id3",
                        "version2",
                        "public",
                        "did something great v2",
                        true,
                        3,
                        "version3"))

        insertChangelog(
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
                        "version3"))

    }

    private fun assertExpectedTestChangelogValues(latestVersion: String, results: List<Changelog>)
    {
        var index = 0

        if (latestVersion == "version3")
        {
            assertChangelogValuesMatch(results[0], "version3", "internal", "everything is broken", false, false)
            assertChangelogValuesMatch(results[1], "version3", "internal", "did something awful v3", false, false)
            assertChangelogValuesMatch(results[2], "version3", "public", "did something great v3", true, true)

            index += 3
        }

        if (latestVersion == "version2" || index > 0)
        {
            assertChangelogValuesMatch(results[index], "version2", "public", "did something great v2", true, true)

            index++
        }

        if (latestVersion == "version1" || index > 0)
        {
            assertChangelogValuesMatch(results[index], "version1", "internal", "did something awful v1", false, false)
            assertChangelogValuesMatch(results[index + 1], "version1", "public", "did something great v1", true, true)

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
            assertChangelogValuesMatch(results[0], "version3", "public", "did something great v3", true, true)

            index++
        }

        if (latestVersion == "version2" || index > 0)
        {
            //The public version of this changelog item is version3, while the 'real' version is version2
            assertChangelogValuesMatch(results[index], "version3", "public", "did something great v2", true, true)

            index++
        }

        Assertions.assertThat(results.count()).isEqualTo(index)

        if ((index == 0) && (latestVersion != "version1"))
        {
            Assertions.fail("Bad test configuration - unexpected report version when checking expected public test changelog values")
        }
    }

    private fun assertChangelogValuesMatch(changelog: Changelog,
                                           report_version: String,
                                           label: String,
                                           value: String,
                                           fromFile: Boolean,
                                           public: Boolean)
    {
        Assertions.assertThat(changelog.reportVersion).isEqualTo(report_version)
        Assertions.assertThat(changelog.fromFile).isEqualTo(fromFile)
        Assertions.assertThat(changelog.label).isEqualTo(label)
        Assertions.assertThat(changelog.value).isEqualTo(value)
        Assertions.assertThat(changelog.public).isEqualTo(public)
    }
}