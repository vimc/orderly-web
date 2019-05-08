package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.jsoup.Jsoup
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.models.Artefact
import org.vaccineimpact.orderlyweb.models.ArtefactFormat
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.xmlmatchers.XmlMatchers.hasXPath
import java.sql.Timestamp

//This will also test the partials which the report-page template includes

class ReportPageTests : TeamcityTests()
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("report-page.ftl")
    }

    private val testReport = ReportVersionDetails(name = "r1",
            displayName = "r1 display",
            id = "r1-v1",
            published = true,
            date = Timestamp(System.currentTimeMillis()).toInstant(),
            author = "an author",
            requester = "a requester",
            description = "description",
            artefacts = listOf(),
            resources = listOf(),
            dataHashes = mapOf())

    private val testArtefactViewModels = listOf(
            ReportController.ArtefactViewModel(
                    Artefact(ArtefactFormat.DATA, "artefact1", listOf()),
                    listOf(
                            ReportController.DownloadableFileViewModel("a1file1.png", "http://a1file1"),
                            ReportController.DownloadableFileViewModel("a1file2.pdf", "http://a1file2")
                    ),
                    "inlinesrc.png"
                ),
            ReportController.ArtefactViewModel(
                    Artefact(ArtefactFormat.DATA, "artefact2", listOf()),
                    listOf(
                            ReportController.DownloadableFileViewModel("a2file1.xls", "http://a2file1")
                    ),
                    null
            )
    )

    private val testDataLinks = listOf(
            ReportController.InputDataViewModel("key1",
                    ReportController.DownloadableFileViewModel("key1.csv", "http://key1/csv"),
                    ReportController.DownloadableFileViewModel("key1.rds", "http://key1/rds")),
            ReportController.InputDataViewModel("key2",
                    ReportController.DownloadableFileViewModel("key2.csv", "http://key2/csv"),
                    ReportController.DownloadableFileViewModel("key2.rds", "http://key2/rds"))
    )

    private val testResources = listOf(
            ReportController.DownloadableFileViewModel("resource1.csv", "http://resource1/csv"),
            ReportController.DownloadableFileViewModel("resource2.csv", "http://resource2/csv")
    )

    // Mock the wrapper serialization in the real model class
    private open class TestReportViewModel(open val reportJson: String,
                                           report: ReportVersionDetails,
                                           focalArtefactUrl: String?,
                                           isAdmin: Boolean,
                                           artefacts: List<ReportController.ArtefactViewModel>,
                                           dataLinks: List<ReportController.InputDataViewModel>,
                                           resources: List<ReportController.DownloadableFileViewModel>,
                                           zipFile: ReportController.DownloadableFileViewModel,
                                           context: ActionContext) :
            ReportController.ReportViewModel(report, focalArtefactUrl, isAdmin, artefacts, dataLinks, resources, zipFile,  context)


    private val mockModel = mock<TestReportViewModel> {
        on { appName } doReturn "testApp"
        on { report } doReturn testReport
        on { reportJson } doReturn "{}"
        on { isAdmin } doReturn false
        on { focalArtefactUrl } doReturn "/testFocalArtefactUrl"
        on { artefacts } doReturn testArtefactViewModels
        on { dataLinks } doReturn testDataLinks
        on { zipFile } doReturn ReportController.DownloadableFileViewModel("zipFileName", "http://zipFileUrl")
    }

    @Test
    fun `renders outline correctly`()
    {
        val xmlResponse = template.xmlResponseFor(mockModel)

        assertThat(xmlResponse, hasXPath("//li[@class='nav-item'][1]/a[@role='tab']/text()",
                equalToCompressingWhiteSpace("Report")))

        assertThat(xmlResponse, hasXPath("//li[@class='nav-item'][2]/a[@role='tab']/text()",
                equalToCompressingWhiteSpace("Downloads")))

        assertThat(xmlResponse, hasXPath("//div[@class='tab-pane active' and @id='report-tab']"))
        assertThat(xmlResponse, hasXPath("//div[@class='tab-pane' and @id='downloads-tab']"))
    }

    @Test
    fun `renders report tab correctly`()
    {
        val xmlResponse = template.xmlResponseFor(mockModel)

        val xPathRoot = "//div[@id='report-tab']"

        assertThat(xmlResponse, hasXPath("$xPathRoot/h1/text()",
                equalToCompressingWhiteSpace("r1 display")))
        assertThat(xmlResponse, hasXPath("$xPathRoot/p[1]/text()",
                equalToCompressingWhiteSpace("r1-v1")))

        assertThat(xmlResponse, hasXPath("$xPathRoot/iframe/@src", equalTo("/testFocalArtefactUrl")))
        assertThat(xmlResponse, hasXPath("$xPathRoot/div[@class='text-right']/a/text()",
                equalToCompressingWhiteSpace("View fullscreen")))
        assertThat(xmlResponse, hasXPath("$xPathRoot/div[@class='text-right']/a/@href", equalTo("/testFocalArtefactUrl")))
    }

    @Test
    fun `renders download tab title correctly`() {
        val xmlResponse = template.xmlResponseFor(mockModel)

        val xPathRoot = "//div[@id='downloads-tab']"

        assertThat(xmlResponse, hasXPath("$xPathRoot/h1/text()",
                equalToCompressingWhiteSpace("r1 display")))

    }

    @Test
    fun `renders download tab artefacts correctly`()
    {
        val stringResponse = template.stringResponseFor(mockModel)
        val jsoupDoc = Jsoup.parse(stringResponse)

        val artefactsEl = jsoupDoc.select("#artefacts")
        val artefactCards = artefactsEl.select(".card")
        Assertions.assertThat(artefactCards.count()).isEqualTo(2)

        val artefactEl1 = artefactCards[0]
        Assertions.assertThat(artefactEl1.select(".card-header").text()).isEqualTo("artefact1")
        Assertions.assertThat(artefactEl1.select("img").attr("src")).isEqualTo("inlinesrc.png")
        val artefact1FileLinks = artefactEl1.select(".card-body div a")

        Assertions.assertThat(artefact1FileLinks.count()).isEqualTo(2)
        Assertions.assertThat(artefact1FileLinks[0].attr("href")).isEqualTo("http://a1file1")
        Assertions.assertThat(artefact1FileLinks[0].text()).isEqualTo("a1file1.png")
        Assertions.assertThat(artefact1FileLinks[0].select("span.download-icon").count()).isEqualTo(1)
        Assertions.assertThat(artefact1FileLinks[1].attr("href")).isEqualTo("http://a1file2")
        Assertions.assertThat(artefact1FileLinks[1].text()).isEqualTo("a1file2.pdf")
        Assertions.assertThat(artefact1FileLinks[1].select("span.download-icon").count()).isEqualTo(1)


        val artefactEl2 = artefactCards[1]
        Assertions.assertThat(artefactEl2.select(".card-header").text()).isEqualTo("artefact2")
        Assertions.assertThat(artefactEl2.select("img").count()).isEqualTo(0)
        val artefact2FileLinks = artefactEl2.select(".card-body div a")
        Assertions.assertThat(artefact2FileLinks.count()).isEqualTo(1)
        Assertions.assertThat(artefact2FileLinks[0].attr("href")).isEqualTo("http://a2file1")
        Assertions.assertThat(artefact2FileLinks[0].text()).isEqualTo("a2file1.xls")
    }

    @Test
    fun `renders download tab data links correctly`()
    {
        val stringResponse = template.stringResponseFor(mockModel)
        val jsoupDoc = Jsoup.parse(stringResponse)

        val linksEl = jsoupDoc.select("#data-links")
        Assertions.assertThat(linksEl.select(".card-header").text()).isEqualTo("Input data to the report")

        val linkRows = linksEl.select(".row")
        Assertions.assertThat(linkRows.count()).isEqualTo(2)

        val linkRow1 = linkRows[0]
        Assertions.assertThat(linkRow1.select(".data-link-key").text()).isEqualTo("key1")
        val linkRow1Csv = linkRow1.select("ul li")[0]
        Assertions.assertThat(linkRow1Csv.select("a").attr("href")).isEqualTo("http://key1/csv")
        Assertions.assertThat(linkRow1Csv.select("a").text()).isEqualTo("key1.csv")
        val linkRow1Rds = linkRow1.select("ul li")[1]
        Assertions.assertThat(linkRow1Rds.select("a").attr("href")).isEqualTo("http://key1/rds")
        Assertions.assertThat(linkRow1Rds.select("a").text()).isEqualTo("key1.rds")

        val linkRow2 = linkRows[1]
        Assertions.assertThat(linkRow2.select(".data-link-key").text()).isEqualTo("key2")
        val linkRow2Csv = linkRow2.select("ul li")[0]
        Assertions.assertThat(linkRow2Csv.select("a").attr("href")).isEqualTo("http://key2/csv")
        Assertions.assertThat(linkRow2Csv.select("a").text()).isEqualTo("key2.csv")
        val linkRow2Rds = linkRow2.select("ul li")[1]
        Assertions.assertThat(linkRow2Rds.select("a").attr("href")).isEqualTo("http://key2/rds")
        Assertions.assertThat(linkRow2Rds.select("a").text()).isEqualTo("key2.rds")
    }

    @Test
    fun `does not render download tab data links if none in model`()
    {
        val testModel = mock<TestReportViewModel> {
            on { appName } doReturn "testApp"
            on { report } doReturn testReport
            on { reportJson } doReturn "{}"
            on { isAdmin } doReturn false
            on { focalArtefactUrl } doReturn "/testFocalArtefactUrl"
            on { dataLinks } doReturn listOf<ReportController.InputDataViewModel>()
            on { zipFile } doReturn ReportController.DownloadableFileViewModel("zipFileName", "http://zipFileUrl")
        }

        val stringResponse = template.stringResponseFor(testModel)
        val jsoupDoc = Jsoup.parse(stringResponse)

        val linksEl = jsoupDoc.select("#data-links")
        Assertions.assertThat(linksEl.count()).isEqualTo(0)
    }

    @Test
    fun `renders resources correctly`()
    {
        val stringResponse = template.stringResponseFor(mockModel)
        val jsoupDoc = Jsoup.parse(stringResponse)

        val resourcesEl = jsoupDoc.select("#resources")
        Assertions.assertThat(resourcesEl.select(".card-header").text()).isEqualTo("Resources")

        val resourceLinks = resourcesEl.select(".card-body div a")
        Assertions.assertThat(resourceLinks.count()).isEqualTo(2)
        Assertions.assertThat(resourceLinks[0].attr("href")).isEqualTo("http://resource1/csv")
        Assertions.assertThat(resourceLinks[0].text()).isEqualTo("resource1.csv")
        Assertions.assertThat(resourceLinks[0].select("span.download-icon").count()).isEqualTo(1)
        Assertions.assertThat(resourceLinks[1].attr("href")).isEqualTo("http://resource2/csv")
        Assertions.assertThat(resourceLinks[1].text()).isEqualTo("resource2.csv")
        Assertions.assertThat(resourceLinks[1].select("span.download-icon").count()).isEqualTo(1)
    }

    @Test
    fun `renders zipfile correctly`()
    {
        val stringResponse = template.stringResponseFor(mockModel)
        val jsoupDoc = Jsoup.parse(stringResponse)

        val zipFileEl = jsoupDoc.select("#zip-file")
        val zipFileLink = zipFileEl.select("")
    }

    @Test
    fun `does not render download tab resources if none in model`()
    {
        val testModel = mock<TestReportViewModel> {
            on { appName } doReturn "testApp"
            on { report } doReturn testReport
            on { reportJson } doReturn "{}"
            on { isAdmin } doReturn false
            on { focalArtefactUrl } doReturn "/testFocalArtefactUrl"
            on { resources } doReturn listOf<ReportController.DownloadableFileViewModel>()
            on { zipFile } doReturn ReportController.DownloadableFileViewModel("zipFileName", "http://zipFileUrl")
        }

        val stringResponse = template.stringResponseFor(testModel)
        val jsoupDoc = Jsoup.parse(stringResponse)

        val resourcesEl = jsoupDoc.select("#resources")
        Assertions.assertThat(resourcesEl.count()).isEqualTo(0)
    }

    @Test
    fun `admins see publish switch`()
    {
        val mockModel = mock<TestReportViewModel> {
            on { appName } doReturn "testApp"
            on { report } doReturn testReport
            on { reportJson } doReturn "{}"
            on { isAdmin } doReturn true
            on { focalArtefactUrl } doReturn "/testFocalArtefactUrl"
            on { zipFile } doReturn ReportController.DownloadableFileViewModel("zipFileName", "http://zipFileUrl")
        }

        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val publishSwitch = htmlResponse.getElementById("publishSwitchVueApp")
        Assertions.assertThat(publishSwitch).isNotNull()
    }

    @Test
    fun `non admins do not see publish switch`()
    {
        val mockModel = mock<TestReportViewModel> {
            on { appName } doReturn "testApp"
            on { report } doReturn testReport
            on { reportJson } doReturn "{}"
            on { isAdmin } doReturn false
            on { focalArtefactUrl } doReturn "/testFocalArtefactUrl"
            on { zipFile } doReturn ReportController.DownloadableFileViewModel("zipFileName", "http://zipFileUrl")
        }

        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val publishSwitch = htmlResponse.getElementById("publishSwitchVueApp")
        Assertions.assertThat(publishSwitch).isNull()
    }
}