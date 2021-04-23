package org.vaccineimpact.orderlyweb.tests.unit_tests.templates.VersionPage

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MetadataTabTests: BaseVersionPageTests()
{
    @Test
    fun `renders metadata tab title correctly`()
    {
        val jsoupDoc = template.jsoupDocFor(VersionPageTestData.testModel)
        val title = jsoupDoc.select("#metadata-tab h1")
        assertThat(title.text()).isEqualToIgnoringWhitespace("r1 display")
    }

    @Test
    fun `renders metadata tab content correctly`()
    {
        val jsoupDoc = template.jsoupDocFor(VersionPageTestData.testModel)
        val content = jsoupDoc.select("#metadata-tab .container")

        assertThat(content.select("#started-label").text()).isEqualTo("Started:")
        assertThat(content.select("#started-value").text()).isEqualTo("Mon 12 Jun 2020 14:23")
        assertThat(content.select("#elapsed-label").text()).isEqualTo("Elapsed:")
        assertThat(content.select("#elapsed-value").text()).isEqualTo("3 hours 2 minutes")

        assertThat(content.select("#git-hr").count()).isEqualTo(1)
        assertThat(content.select("#git-branch-label").text()).isEqualTo("Git branch:")
        assertThat(content.select("#git-branch-value").text()).isEqualTo("master")
        assertThat(content.select("#git-commit-label").text()).isEqualTo("Git commit:")
        assertThat(content.select("#git-commit-value").text()).isEqualTo("abc123")
    }

    @Test
    fun `renders no git or db instance elements if properties not set`()
    {
        val basicReportVersion = VersionPageTestData.testBasicReportVersion.copy(gitBranch = null, gitCommit = null)
        val testModel = VersionPageTestData.testModel.copy(basicReportVersion)

        val jsoupDoc = template.jsoupDocFor(testModel)
        val content = jsoupDoc.select("#metadata-tab .container")
        assertThat(content.select("#git-hr").count()).isEqualTo(0)
        assertThat(content.select("#git-branch-row").count()).isEqualTo(0)
        assertThat(content.select("#git-commit-row").count()).isEqualTo(0)
        assertThat(content.select(".db-instance-row").count()).isEqualTo(0)
    }

    @Test
    fun `renders correct git elements if only branch is set`()
    {
        val basicReportVersion = VersionPageTestData.testBasicReportVersion.copy(gitBranch = "master", gitCommit = null)
        val testModel = VersionPageTestData.testModel.copy(basicReportVersion)

        val jsoupDoc = template.jsoupDocFor(testModel)
        val content = jsoupDoc.select("#metadata-tab .container")
        assertThat(content.select("#git-hr").count()).isEqualTo(1)
        assertThat(content.select("#git-branch-label").text()).isEqualTo("Git branch:")
        assertThat(content.select("#git-branch-value").text()).isEqualTo("master")
        assertThat(content.select("#git-commit-row").count()).isEqualTo(0)
    }

    @Test
    fun `renders correct git elements if only commit is set`()
    {
        val basicReportVersion = VersionPageTestData.testBasicReportVersion.copy(gitBranch = null, gitCommit = "abc123")
        val testModel = VersionPageTestData.testModel.copy(basicReportVersion)

        val jsoupDoc = template.jsoupDocFor(testModel)
        val content = jsoupDoc.select("#metadata-tab .container")
        assertThat(content.select("#git-hr").count()).isEqualTo(1)
        assertThat(content.select("#git-branch-row").count()).isEqualTo(0)
        assertThat(content.select("#git-commit-label").text()).isEqualTo("Git commit:")
        assertThat(content.select("#git-commit-value").text()).isEqualTo("abc123")
    }

    @Test
    fun `renders database instances alongside git elements`()
    {
        val basicReportVersion = VersionPageTestData.testBasicReportVersion.copy(gitBranch = "master", gitCommit = "abc123")
        val testModel = VersionPageTestData.testModel.copy(basicReportVersion, instances = mapOf("p1" to "v1", "p2" to "v2"))
        val jsoupDoc = template.jsoupDocFor(testModel)

        val content = jsoupDoc.select("#metadata-tab .container")
        assertThat(content.select("#git-hr").count()).isEqualTo(1)
        assertThat(content.select("#git-branch-label").text()).isEqualTo("Git branch:")
        assertThat(content.select("#git-branch-value").text()).isEqualTo("master")
        assertThat(content.select("#git-commit-label").text()).isEqualTo("Git commit:")
        assertThat(content.select("#git-commit-value").text()).isEqualTo("abc123")
        val content2 = jsoupDoc.select(".db-instance-row")
        assertThat(content2.select(".db-instance-row").count()).isEqualTo(2)
        assertThat(content2.select(".db-instance-label")[0].text()).isEqualTo("Database \"p1\":")
        assertThat(content2.select(".db-instance-value")[0].text()).isEqualTo("v1")
        assertThat(content2.select(".db-instance-label")[1].text()).isEqualTo("Database \"p2\":")
        assertThat(content2.select(".db-instance-value")[1].text()).isEqualTo("v2")
    }

    @Test
    fun `renders database instances but not git elements if branch and commit are not set`()
    {
        val basicReportVersion = VersionPageTestData.testBasicReportVersion.copy(gitBranch = null, gitCommit = null)
        val testModel = VersionPageTestData.testModel.copy(basicReportVersion, instances = mapOf("p1" to "v1", "p2" to "v2"))
        val jsoupDoc = template.jsoupDocFor(testModel)

        val content = jsoupDoc.select("#metadata-tab .container")
        assertThat(content.select("#git-hr").count()).isEqualTo(1)
        assertThat(content.select("#git-branch-row").count()).isEqualTo(0)
        assertThat(content.select("#git-commit-row").count()).isEqualTo(0)
        val content2 = jsoupDoc.select(".db-instance-row")
        assertThat(content2.select(".db-instance-row").count()).isEqualTo(2)
        assertThat(content2.select(".db-instance-label")[0].text()).isEqualTo("Database \"p1\":")
        assertThat(content2.select(".db-instance-value")[0].text()).isEqualTo("v1")
        assertThat(content2.select(".db-instance-label")[1].text()).isEqualTo("Database \"p2\":")
        assertThat(content2.select(".db-instance-value")[1].text()).isEqualTo("v2")
    }

    @Test
    fun `renders dependencies component`()
    {
        val jsoupDoc = template.jsoupDocFor(VersionPageTestData.testModel)
        val app = jsoupDoc.select("#reportDependenciesVueApp")
        assertThat(app.select("report-dependencies").count()).isEqualTo(1)
        assertThat(app.select("report-dependencies").attr(":report")).isEqualTo("report")
    }
}
