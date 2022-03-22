package org.vaccineimpact.orderlyweb.tests.unit_tests.templates.VersionPage

import org.assertj.core.api.Assertions
import org.junit.Test

class ReportTabTests : BaseVersionPageTests()
{
    @Test
    fun `renders report tab correctly`()
    {
        val jsoupDoc = template.jsoupDocFor(VersionPageTestData.testModel)
        val tab = jsoupDoc.select("#report-tab")

        Assertions.assertThat(tab.select("h1").text()).isEqualToIgnoringWhitespace("r1 display")
        Assertions.assertThat(tab.select("p:first-of-type").text()).isEqualToIgnoringWhitespace("r1-v1")

        Assertions.assertThat(tab.select("#report-name").text()).isEqualToIgnoringWhitespace("r1")
        Assertions.assertThat(tab.select("#report-description").text()).isEqualToIgnoringWhitespace("r1 description")
        Assertions.assertThat(tab.select("#report-parameters").text()).isEqualToIgnoringWhitespace("p1=v1, p2=v2")

        Assertions.assertThat(tab.select("iframe").attr("src")).isEqualTo("/testFocalArtefactUrl")
        Assertions.assertThat(tab.select("div.text-right a").text()).isEqualToIgnoringWhitespace("View fullscreen")
        Assertions.assertThat(tab.select("div.text-right a").attr("href")).isEqualToIgnoringWhitespace("/testFocalArtefactUrl")
    }

    @Test
    fun `renders report tags correctly`()
    {
        val readerDoc = template.jsoupDocFor(VersionPageTestData.testModel)

        var tagsDiv = readerDoc.select("#reportTagsVueApp")
        var tagsEl = tagsDiv.select("report-tags")
        Assertions.assertThat(tagsEl.attr(":can-edit")).isEqualTo("false")
        Assertions.assertThat(tagsEl.attr(":report")).isEqualTo("report")

        val appViewModel = VersionPageTestData.testDefaultModel.copy(isReviewer = true)
        val model = VersionPageTestData.testModel.copy(appViewModel = appViewModel)
        val reviewerDoc = template.jsoupDocFor(model)

        tagsDiv = reviewerDoc.select("#reportTagsVueApp")
        tagsEl = tagsDiv.select("report-tags")
        Assertions.assertThat(tagsEl.attr(":can-edit")).isEqualTo("true")
        Assertions.assertThat(tagsEl.attr(":report")).isEqualTo("report")
    }

    @Test
    fun `does not render report name if identical to display name`()
    {
        val basicReport = VersionPageTestData.testBasicReportVersion.copy(displayName = "r1")
        val report = VersionPageTestData.testModel.copy(report = basicReport)

        val jsoupDoc = template.jsoupDocFor(report)
        val name = jsoupDoc.select("#report-tab #report-name")
        Assertions.assertThat(name.count()).isEqualTo(0)
    }

    @Test
    fun `does not render report description if null`()
    {
        val basicReport = VersionPageTestData.testBasicReportVersion.copy(description = null)
        val report = VersionPageTestData.testModel.copy(report = basicReport)

        val jsoupDoc = template.jsoupDocFor(report)
        val desc = jsoupDoc.select("#report-tab #report-description")
        Assertions.assertThat(desc.count()).isEqualTo(0)
    }
}
