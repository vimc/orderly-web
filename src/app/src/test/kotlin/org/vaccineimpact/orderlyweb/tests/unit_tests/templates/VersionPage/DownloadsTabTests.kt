package org.vaccineimpact.orderlyweb.tests.unit_tests.templates.VersionPage

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.FreeMarkerTest

class DownloadsTabTests: FreeMarkerTest("report-page.ftl")
{
    @Test
    fun `renders download tab title correctly`()
    {
        val jsoupDoc = jsoupDocFor(VersionPageTestData.testModel)
        val title = jsoupDoc.select("#downloads-tab h1")
        assertThat(title.text()).isEqualToIgnoringWhitespace("r1 display")
    }

    @Test
    fun `renders download tab artefacts correctly`()
    {
        val jsoupDoc = jsoupDocFor(VersionPageTestData.testModel)

        val artefactsEl = jsoupDoc.select("#artefacts")
        val artefactCards = artefactsEl.select(".card")
        assertThat(artefactCards.count()).isEqualTo(2)

        val artefactEl1 = artefactCards[0]
        assertThat(artefactEl1.select(".card-header").text()).isEqualTo("artefact1")
        assertThat(artefactEl1.select("img").attr("src")).isEqualTo("inlinesrc.png")
        val artefact1FileLinks = artefactEl1.select(".card-body div a")
        val artefact1FileSizeSpans = artefactEl1.select("span.file-size")

        assertThat(artefact1FileLinks.count()).isEqualTo(2)
        assertThat(artefact1FileSizeSpans.count()).isEqualTo(2)

        assertThat(artefact1FileLinks[0].attr("href")).isEqualTo("http://a1file1")
        assertThat(artefact1FileLinks[0].text()).isEqualTo("a1file1.png")
        assertThat(artefact1FileLinks[0].select("span.download-icon").count()).isEqualTo(1)
        assertThat(artefact1FileSizeSpans[0].text()).isEqualTo("(19 KB)")

        assertThat(artefact1FileLinks[1].attr("href")).isEqualTo("http://a1file2")
        assertThat(artefact1FileLinks[1].text()).isEqualTo("a1file2.pdf")
        assertThat(artefact1FileLinks[1].select("span.download-icon").count()).isEqualTo(1)
        assertThat(artefact1FileSizeSpans[1].text()).isEqualTo("(123 bytes)")


        val artefactEl2 = artefactCards[1]
        assertThat(artefactEl2.select(".card-header").text()).isEqualTo("artefact2")
        assertThat(artefactEl2.select("img").count()).isEqualTo(0)

        val artefact2FileLinks = artefactEl2.select(".card-body div a")
        val artefact2FileSizeSpans = artefactEl2.select("span.file-size")
        assertThat(artefact2FileLinks.count()).isEqualTo(1)
        assertThat(artefact2FileSizeSpans.count()).isEqualTo(1)

        assertThat(artefact2FileLinks[0].attr("href")).isEqualTo("http://a2file1")
        assertThat(artefact2FileLinks[0].text()).isEqualTo("a2file1.xls")
        assertThat(artefact2FileSizeSpans[0].text()).isEqualTo("(2.2 MB)")
    }

    @Test
    fun `renders download tab data links correctly`()
    {
        val jsoupDoc = jsoupDocFor(VersionPageTestData.testModel)

        val linksEl = jsoupDoc.select("#data-links")
        assertThat(linksEl.select(".card-header").text()).isEqualTo("Input data to the report")

        val linkRows = linksEl.select(".row")
        assertThat(linkRows.count()).isEqualTo(2)

        val linkRow1 = linkRows[0]
        assertThat(linkRow1.select(".data-link-key").text()).isEqualTo("key1")

        val linkRow1Csv = linkRow1.select("ul li")[0]
        assertThat(linkRow1Csv.select("a").attr("href")).isEqualTo("http://key1/csv")
        assertThat(linkRow1Csv.select("a").text()).isEqualTo("key1.csv")
        //Currently failing because of weird commons ui rounding behaviour...
        assertThat(linkRow1Csv.select("span.file-size").text()).isEqualTo("(1.6 MB)")

        val linkRow1Rds = linkRow1.select("ul li")[1]
        assertThat(linkRow1Rds.select("a").attr("href")).isEqualTo("http://key1/rds")
        assertThat(linkRow1Rds.select("a").text()).isEqualTo("key1.rds")
        assertThat(linkRow1Rds.select("span.file-size").text()).isEqualTo("(3.9 MB)")

        val linkRow2 = linkRows[1]
        assertThat(linkRow2.select(".data-link-key").text()).isEqualTo("key2")
        val linkRow2Csv = linkRow2.select("ul li")[0]
        assertThat(linkRow2Csv.select("a").attr("href")).isEqualTo("http://key2/csv")
        assertThat(linkRow2Csv.select("a").text()).isEqualTo("key2.csv")
        assertThat(linkRow2Csv.select("span.file-size").text()).isEqualTo("(3.0 KB)")

        val linkRow2Rds = linkRow2.select("ul li")[1]
        assertThat(linkRow2Rds.select("a").attr("href")).isEqualTo("http://key2/rds")
        assertThat(linkRow2Rds.select("a").text()).isEqualTo("key2.rds")
        assertThat(linkRow2Rds.select("span.file-size").text()).isEqualTo("(4.5 KB)")
    }

    @Test
    fun `does not render download tab data links if none in model`()
    {
        val testModel = VersionPageTestData.testModel.copy(dataLinks = listOf())

        val jsoupDoc = jsoupDocFor(testModel)

        val linksEl = jsoupDoc.select("#data-links")
        assertThat(linksEl.count()).isEqualTo(0)
    }

    @Test
    fun `renders resources correctly`()
    {
        val jsoupDoc = jsoupDocFor(VersionPageTestData.testModel)

        val resourcesEl = jsoupDoc.select("#resources")
        assertThat(resourcesEl.select(".card-header").text()).isEqualTo("Resources")

        val resourceLinks = resourcesEl.select(".card-body div a")
        val resourceFileSizeSpans = resourcesEl.select(".card-body div span.file-size")

        assertThat(resourceLinks.count()).isEqualTo(2)
        assertThat(resourceFileSizeSpans.count()).isEqualTo(2)

        assertThat(resourceLinks[0].attr("href")).isEqualTo("http://resource1/csv")
        assertThat(resourceLinks[0].text()).isEqualTo("resource1.csv")
        assertThat(resourceLinks[0].select("span.download-icon").count()).isEqualTo(1)
        assertThat(resourceFileSizeSpans[0].text()).isEqualTo("(1.2 KB)")

        assertThat(resourceLinks[1].attr("href")).isEqualTo("http://resource2/csv")
        assertThat(resourceLinks[1].text()).isEqualTo("resource2.csv")
        assertThat(resourceLinks[1].select("span.download-icon").count()).isEqualTo(1)
        assertThat(resourceFileSizeSpans[1].text()).isEqualTo("(2.3 KB)")
    }

    @Test
    fun `renders zipfile correctly`()
    {
        val jsoupDoc = jsoupDocFor(VersionPageTestData.testModel)

        val zipFileEl = jsoupDoc.select("#zip-file")
        val zipFileLink = zipFileEl.select("a")
        assertThat(zipFileLink.attr("href")).isEqualTo("http://zipFileUrl")
        assertThat(zipFileLink.text()).isEqualTo("zipFileName")
        assertThat(zipFileLink.select("span.download-icon").count()).isEqualTo(1)
        //should not have rendered size link, as no size info
        assertThat(zipFileEl.select("span.file-link").count()).isEqualTo(0)
    }

    @Test
    fun `does not render download tab resources if none in model`()
    {
        val testModel = VersionPageTestData.testModel.copy(resources = listOf())
        val jsoupDoc = jsoupDocFor(testModel)

        val resourcesEl = jsoupDoc.select("#resources")
        assertThat(resourcesEl.count()).isEqualTo(0)
    }
}
