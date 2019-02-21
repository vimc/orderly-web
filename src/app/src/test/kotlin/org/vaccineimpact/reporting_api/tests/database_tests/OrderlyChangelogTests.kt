package org.vaccineimpact.reporting_api.tests.database_tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.api.models.Changelog
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import org.vaccineimpact.reporting_api.tests.ChangelogWithPublicVersion
import org.vaccineimpact.reporting_api.tests.insertReport

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

        insertReport("anothertest", "anotherversion1", changelog = listOf(
                ChangelogWithPublicVersion("anotherversion1", "public","did something great v1", true),
                ChangelogWithPublicVersion("anotherversion1","internal","did something awful v1", false)))

        val sut = createSut(true)

        val results = sut.getChangelogByNameAndVersion("test", "version2")

        assertExpectedTestChangelogValues("version2", results)
    }

    @Test
    fun `reader does not get changelog for other reports`()
    {
        insertTestReportChangelogs()

        insertReport("anothertest", "anotherversion1", changelog = listOf(
                ChangelogWithPublicVersion("anotherversion1", "public","did something great v1", true),
                ChangelogWithPublicVersion("anotherversion1","internal","did something awful v1", false)))

        val sut = createSut(false)

        val results = sut.getChangelogByNameAndVersion("test", "version3")

        assertExpectedPublicTestChangelogValues("version3", results)
    }

    @Test
    fun `can get empty changelog`()
    {
        insertReport("emptytest", "version1", changelog = listOf())

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

        insertReport("anothertest", "anotherversion1", changelog = listOf(
                ChangelogWithPublicVersion("anotherversion1", "public","did something great v1", true, "anotherversion1"),
                ChangelogWithPublicVersion("anotherversion1","internal","did something awful v1", false, "anotherversion1")))

        val sut = createSut(true)

        val results = sut.getLatestChangelogByName("test")

        assertExpectedTestChangelogValues("version3", results)
    }

    @Test
    fun `reader does not get latest changelog for other reports`()
    {
        insertTestReportChangelogs()

        insertReport("anothertest", "anotherversion1", changelog = listOf(
                ChangelogWithPublicVersion("anotherversion1", "public","did something great v1", true, "anotherversion1"),
                ChangelogWithPublicVersion("anotherversion1","internal","did something awful v1", false, "anotherversion1")))

        val sut = createSut(false)

        val results = sut.getLatestChangelogByName("test")

        assertExpectedPublicTestChangelogValues("version3", results)
    }

    @Test
    fun `reader get latest changelog gets latest published version's changelog`()
    {
        //Should not get changelog for latest version if it is not public
        //but should get public changelog items for versions previous to latest
        //published version, even id those old versions were unpublished
        //as long as they have report version public values
        insertReport("test", "v1", published = false, changelog = listOf(
                ChangelogWithPublicVersion("v1", "public","did something great v1", true, "v2"),
                //This changelog has a public version set, but should not be returned as it is marke internal
                ChangelogWithPublicVersion("v1","internal","did something awful v1", false)))
        insertReport("test", "v2", published = true, changelog = listOf(
                ChangelogWithPublicVersion("v2", "public","did something great v2", true, "v2"),
                ChangelogWithPublicVersion("v2","internal","did something awful v2", false, "v2")))
        insertReport("test", "v3",  published =  false,
                changelog = listOf(
                        //Set a public version here just for testing - this version should not be considered the latest for
                        //a reader, so its changelog items should not be returned, even if they have a public version set
                        ChangelogWithPublicVersion("v3", "public","did something great v3", true, "v3"),
                        ChangelogWithPublicVersion("v3","internal","did something awful v3", false)))

        val sut = createSut(false)

        val results = sut.getLatestChangelogByName("test")

        Assertions.assertThat(results.count()).isEqualTo(2)
        //v2 - published
        assertChangelogValuesMatch(results[0], "v2", "public", "did something great v2", true)
        //v1 - unpublished, but public version marked as v2
        assertChangelogValuesMatch(results[1], "v2", "public", "did something great v1", true)
    }

    private fun insertTestReportChangelogs()
    {
        insertReport("test", "version1", changelog = listOf(
                ChangelogWithPublicVersion("version1", "public","did something great v1", true, null),
                ChangelogWithPublicVersion("version1","internal","did something awful v1", false, null)))

        insertReport("test", "version2",
                published = false, //This version is unpublished but its item is public so both readers and reviewers should see it
                changelog = listOf(
                        ChangelogWithPublicVersion("version2", "public","did something great v2", true, "version3")))

        insertReport("test", "version3", changelog = listOf(
                ChangelogWithPublicVersion("version3", "public","did something great v3", true, "version3"),
                ChangelogWithPublicVersion("version3","internal","did something awful v3", false, "version3"),
                ChangelogWithPublicVersion("version3", "technical", "everything is broken", false, "version3")))
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

            index ++
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