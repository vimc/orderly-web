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
    fun `renders no git elements if git properties not set`()
    {
        val basicReportVersion = VersionPageTestData.testBasicReportVersion.copy(gitBranch = null, gitCommit = null)
        val testModel = VersionPageTestData.testModel.copy(basicReportVersion)

        val jsoupDoc = template.jsoupDocFor(testModel)
        val content = jsoupDoc.select("#metadata-tab .container")
        assertThat(content.select("#git-hr").count()).isEqualTo(0)
        assertThat(content.select("#git-branch-row").count()).isEqualTo(0)
        assertThat(content.select("#git-commit-row").count()).isEqualTo(0)
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
    fun `renders dependencies component`()
    {
        val jsoupDoc = template.jsoupDocFor(VersionPageTestData.testModel)
        val app = jsoupDoc.select("#reportDependenciesVueApp")
        assertThat(app.select("report-dependencies").count()).isEqualTo(1)
        assertThat(app.select("report-dependencies").attr(":report")).isEqualTo("report")
    }
}
